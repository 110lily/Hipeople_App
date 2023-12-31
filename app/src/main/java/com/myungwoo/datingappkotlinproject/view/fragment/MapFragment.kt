package com.myungwoo.datingappkotlinproject.view.fragment

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.drawable.BitmapDrawable
import android.location.Address
import android.location.Geocoder
import android.location.Location
import android.location.LocationManager
import android.os.Bundle
import android.os.Looper
import android.provider.Settings
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import androidx.viewpager2.widget.ViewPager2
import com.google.android.gms.common.api.Status
import com.google.android.gms.location.*
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.widget.AutocompleteSupportFragment
import com.google.android.libraries.places.widget.listener.PlaceSelectionListener
import com.google.android.material.snackbar.Snackbar
import com.myungwoo.datingappkotlinproject.BuildConfig
import com.myungwoo.datingappkotlinproject.adapter.CustomAdapter
import com.myungwoo.datingappkotlinproject.R
import com.myungwoo.datingappkotlinproject.databinding.FragmentMapBinding
import com.myungwoo.datingappkotlinproject.view.activity.AppMainActivity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import noman.googleplaces.*
import java.io.IOException
import java.util.*

class MapFragment : Fragment(), OnMapReadyCallback,
    ActivityCompat.OnRequestPermissionsResultCallback, PlacesListener {

    private lateinit var binding: FragmentMapBinding
    private lateinit var appMainActivity: AppMainActivity
    private var searchLATLNG = LatLng(0.0, 0.0) // 검색 위치 초기화
    private var mMap: GoogleMap? = null
    private var currentMarker: Marker? = null
    private var needRequest = false // 위치 업데이트 요청 필요 여부
    private var previousMarker: MutableList<Marker>? = null
    private var currentflag = 0 // 현재 플래그 초기화
    private var searchflag = 0 // 검색 플래그 초기화
    private var circle: Circle? = null // 원형 영역 표시용 Circle
    private var circle1KM: CircleOptions? = null // 1KM 원형 영역 설정용 CircleOptions
    private var mCurrentLocatiion: Location? = null // 현재 위치 정보 초기화
    private var currentPosition: LatLng? = null // 현재 위치의 위도와 경도 초기화
    private var mFusedLocationClient: FusedLocationProviderClient? = null // 위치 서비스 클라이언트
    private var locationRequest: LocationRequest? = null // 위치 업데이트 요청
    private var location: Location? = null // 위치 정보
    private var mLayout: View? = null // Snackbar 사용을 위한 View

    // 필요한 퍼미션 정의
    private var requiredPermissions = arrayOf(
        Manifest.permission.ACCESS_FINE_LOCATION,
        Manifest.permission.ACCESS_COARSE_LOCATION
    )

    override fun onAttach(context: Context) {
        super.onAttach(context)
        appMainActivity = context as AppMainActivity
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentMapBinding.inflate(inflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        mLayout = binding.layoutMain

        // 화면 켜진 상태 유지
        requireActivity().window.setFlags(
            WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON,
            WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
        )

        // 위치 퍼미션 요청
        if (isLocationPermissionRequired()) {
            checkPermissions()
        }

        // 위치 정보 업데이트 요청 설정
        setupLocationUpdates()

        //음식점 버튼 클릭시
        binding.btnRestaurant.setOnClickListener {
            Toast.makeText(context, "조금만 기다려주세요! 곧 지도를 불러오겠습니다^^", Toast.LENGTH_SHORT).show()
            if (currentPosition != null) {
                lifecycleScope.launch(Dispatchers.Default) {
                    withContext(Dispatchers.Main) {
                        // UI 업데이트는 메인 스레드에서 수행
                        showRestInformation(currentPosition!!, searchLATLNG)
                    }
                }
            } else {
                // 위치 정보가 없는 경우 처리
                Toast.makeText(context, "위치 정보가 없습니다. 위치정보에서 Hipeople앱을 허용해주세요", Toast.LENGTH_SHORT).show()
                checkPermissions()
            }
        }

        //카페 버튼 클릭시
        binding.btnCafe.setOnClickListener {
            Toast.makeText(context, "조금만 기다려주세요! 곧 지도를 불러오겠습니다^^", Toast.LENGTH_SHORT).show()
            if (currentPosition != null) {
                lifecycleScope.launch(Dispatchers.Default) {
                    withContext(Dispatchers.Main) {
                        // UI 업데이트는 메인 스레드에서 수행
                        showCafeInformation(currentPosition!!, searchLATLNG)
                    }
                }
            } else {
                // 위치 정보가 없는 경우 처리
                Toast.makeText(context, "위치 정보가 없습니다. 위치정보에서 Hipeople앱을 허용해주세요 ", Toast.LENGTH_SHORT).show()
                checkPermissions()
            }
        }


    }


    //현재 화면에 따라 위치 권한이 필요한지 여부를 판단하는 역할을 합니다.
    private fun isLocationPermissionRequired(): Boolean {
        val viewPager = requireActivity().findViewById<ViewPager2>(R.id.viewPager2)
        val currentFragment = (viewPager.adapter as? CustomAdapter)?.createFragment(viewPager.currentItem)

        return currentFragment is MapFragment
    }

    private fun checkPermissions() {
        //정확한 위치정보
        val fineLocationPermissionGranted = ActivityCompat.checkSelfPermission(
            requireContext(), Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        //대략적인 위치정보
        val coarseLocationPermissionGranted = ActivityCompat.checkSelfPermission(
            requireContext(), Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        // 이미 권한이 부여된 경우
        if (fineLocationPermissionGranted && coarseLocationPermissionGranted) {
            startLocationUpdates()
            return
        }

        // 권한 요청 다이얼로그 표시
        val builder = AlertDialog.Builder(requireContext())
        builder.setTitle("위치 정보 요청")
        builder.setMessage("앱에서 위치 정보를 사용하려고 합니다. 이 기능을 활성화하면 주변 맛집과 카페를 추천받을 수 있습니다.")
        builder.setPositiveButton("동의") { _, _ ->
            requestPermissions(requiredPermissions, PERMISSIONS_REQUEST_CODE)
        }
        builder.setNegativeButton("거부") { _, _ ->
            Snackbar.make(
                mLayout!!, "거부 되었습니다.", Snackbar.LENGTH_INDEFINITE
            ).show()
        }
        builder.create().show()
    }

    private fun setupLocationUpdates() {
        // 위치 정보 업데이트 요청 설정
        locationRequest = LocationRequest()
            .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
            .setInterval(120000) // 업데이트 주기 (2분)
            .setFastestInterval(110000) // 가장 빠른 업데이트 주기 (1분)

        val builder = LocationSettingsRequest.Builder()
        builder.addLocationRequest(locationRequest!!)

        // 위치 정보 제공 서비스 초기화
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity())

        // SupportMapFragment 객체 초기화 및 OnMapReadyCallback 인터페이스 확인
        val mapFragment = childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment?
        mapFragment!!.getMapAsync(this)

        // 이전에 추가한 마커들을 저장하기 위한 ArrayList 객체 초기화
        previousMarker = ArrayList()
    }

    // 지도와 위치 관련 설정, 그리고 검색 자동완성 기능을 초기화하는 부분
    override fun onMapReady(googleMap: GoogleMap) {
        Log.d(TAG, "onMapReady :")
        mMap = googleMap
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(appMainActivity)
        updateLocation()

        // AutocompleteSupportFragment 초기화 검색기능에서 자동완성기능을 추가
        val autocompleteFragment =
            childFragmentManager.findFragmentById(R.id.autocomplete_fragment)
                    as AutocompleteSupportFragment
        //검색결과로 반환받을 정보를 지정
        autocompleteFragment.setPlaceFields(
            listOf(
                com.google.android.libraries.places.api.model.Place.Field.ID,
                com.google.android.libraries.places.api.model.Place.Field.NAME,
                com.google.android.libraries.places.api.model.Place.Field.LAT_LNG
            )
        )
        //검색결과 선택시 onplaceselected함수호출
        autocompleteFragment.setOnPlaceSelectedListener(object : PlaceSelectionListener {
            override fun onPlaceSelected(p0: com.google.android.libraries.places.api.model.Place) {
                //검색후 선택된 장소의 위도 경도정보를 생성
                val location = Location("")
                location.latitude = p0.latLng.latitude
                location.longitude = p0.latLng.longitude
                //검색된 위치에 마커표시
                searchLATLNG = LatLng(location.latitude, location.longitude)
                val markerOptions = MarkerOptions().position(searchLATLNG)
                //카메라이동
                val cameraPosition =
                    CameraPosition.Builder().target(searchLATLNG).zoom(15.0f).build()
                mMap?.clear()
                mMap?.addMarker(markerOptions)
                mMap?.moveCamera(CameraUpdateFactory.newCameraPosition(cameraPosition))
                searchflag = 2
            }

            override fun onError(status: Status) {
                // 검색 결과를 처리하지 못한 경우의 코드 작성
                Log.e(TAG, "An error occurred: ${status.statusMessage}")
            }
        })
        //지도의 초기위치를 서울로 이동
        setDefaultLocation()

        // 2. 이미 퍼미션을 가지고 있다면
        // ( 안드로이드 6.0 이하 버전은 런타임 퍼미션이 필요없기 때문에 이미 허용된 걸로 인식합니다.)
        startLocationUpdates()                                                                                                                                                                                                                                                                                                                            // 3. 위치 업데이트 시작

        mMap!!.uiSettings.isMyLocationButtonEnabled = true
        mMap!!.setOnMapClickListener {
            Log.d(
                TAG,
                "onMapClick :"
            )
        }
        Places.initialize(appMainActivity, BuildConfig.googleMap_Key)
    }

    @SuppressLint("MissingPermission")
    //updateLocation() 함수에서는 위치 정보 요청 객체(LocationRequest)를 생성하고, 위치 업데이트 콜백(LocationCallback)을 등록합니다.
    // 위치 요청 객체에서는 위치 정보의 정확도와 업데이트 간격을 설정하고, requestLocationUpdates() 함수를 호출하여 위치 정보를 요청합니다.
    fun updateLocation() {
        val locationRequest = LocationRequest.create()
        locationRequest.run {
            priority = LocationRequest.PRIORITY_HIGH_ACCURACY
            interval = 1000
        }
        mFusedLocationClient?.requestLocationUpdates(
            locationRequest,
            locationCallback,
            Looper.myLooper()
        )

    }

    //위치정보 업데이트 결과를 처리
    var locationCallback: LocationCallback = object : LocationCallback() {
        override fun onLocationResult(locationResult: LocationResult) {
            super.onLocationResult(locationResult)
            val locationList = locationResult.locations
            if (locationList.size > 0) {
                location = locationList[locationList.size - 1]
                currentPosition = LatLng(location!!.latitude, location!!.longitude)
                //위치정보가 있을경우 마커타이틀,스니펫설정
                val markerTitle = getCurrentAddress(currentPosition!!)
                val markerSnippet =
                    "위도:" + location!!.latitude.toString() + " 경도:" + location!!.longitude.toString()
                Log.d(TAG, "onLocationResult : $markerSnippet")
                searchflag = 1

                //setcurrentLocation 함수를 호출하여 현재 위치에 마커 생성하고 이동
                setCurrentLocation(location, markerTitle, markerSnippet)
                mCurrentLocatiion = location
            }
        }
    }

    private fun startLocationUpdates() {
        //위치 서비스 사용가능 여부를 확인
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(appMainActivity)
        if (!checkLocationServicesStatus()) {
            Log.d(TAG, "startLocationUpdates : call showDialogForLocationServiceSetting")
            //사용 불가능할경우 showDialogForLocationServiceSetting() 함수를 호출하여 사용자에게 위치 서비스 활성화를 요청합니다.
            showDialogForLocationServiceSetting()
            //위치 서비스 사용 가능한 경우, 두개의 권한이있는지확인 권한이없는경우 함수를 종료
        } else {
            val hasFineLocationPermission = ContextCompat.checkSelfPermission(
                appMainActivity,
                Manifest.permission.ACCESS_FINE_LOCATION
            )
            val hasCoarseLocationPermission = ContextCompat.checkSelfPermission(
                appMainActivity,
                Manifest.permission.ACCESS_COARSE_LOCATION
            )
            if (hasFineLocationPermission != PackageManager.PERMISSION_GRANTED ||
                hasCoarseLocationPermission != PackageManager.PERMISSION_GRANTED
            ) {
                Log.d(TAG, "startLocationUpdates : 퍼미션 안가지고 있음")
                return
            }
            Log.d(TAG, "startLocationUpdates : call mFusedLocationClient.requestLocationUpdates")
            //위치권한있는경우 requestLocationUpdates함수를 호출하여 위치정보를요청
            //이후 checkPermission 함수를 호출하여 권한이있는경우
            //mMap!!.isMyLocationEnabled = true 현재위치버튼활성화
            mFusedLocationClient!!.requestLocationUpdates(
                locationRequest!!,
                locationCallback,
                Looper.myLooper()
            )
            if (checkPermission() && mMap != null) {
                mMap?.isMyLocationEnabled = true
            }
        }
    }

    override fun onStart() {
        super.onStart()
        Log.d(TAG, "onStart")
        if (checkPermission()) {
            Log.d(TAG, "onStart : call mFusedLocationClient.requestLocationUpdates")
            mFusedLocationClient!!.requestLocationUpdates(locationRequest!!, locationCallback, null)
            if (mMap != null) mMap!!.isMyLocationEnabled = true
        }
    }

    override fun onStop() {
        super.onStop()
        if (mFusedLocationClient != null) {
            Log.d(TAG, "onStop : call stopLocationUpdates")
            mFusedLocationClient!!.removeLocationUpdates(locationCallback)
        }
    }

    fun getCurrentAddress(latlng: LatLng): String {

        //지오코더... GPS를 주소로 변환
        val geocoder = Geocoder(appMainActivity, Locale.getDefault())
        val addresses: List<Address>?
        try {
            addresses = geocoder.getFromLocation(
                latlng.latitude,
                latlng.longitude,
                1
            )
        } catch (ioException: IOException) {
            //네트워크 문제
            Toast.makeText(appMainActivity, "지오코더 서비스 사용불가", Toast.LENGTH_LONG).show()
            return "지오코더 서비스 사용불가"

        } catch (illegalArgumentException: IllegalArgumentException) {
            Toast.makeText(appMainActivity, "잘못된 GPS 좌표", Toast.LENGTH_LONG).show()
            return "잘못된 GPS 좌표"
        }
        if (addresses == null || addresses.size == 0) {
            //Toast.makeText(this, "주소미발견", Toast.LENGTH_LONG).show()
            return ""
        } else {
            val address = addresses[0]
            return address.getAddressLine(0).toString()
        }
    }

    private fun checkLocationServicesStatus(): Boolean {
        val locationManager =
            requireActivity().getSystemService(Context.LOCATION_SERVICE) as LocationManager
        return (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
                || locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER))
    }

    fun setCurrentLocation(location: Location?, markerTitle: String?, markerSnippet: String?) {
        if (currentMarker != null) currentMarker!!.remove()
        val currentLatLng = LatLng(location!!.latitude, location.longitude)
        val markerOptions = MarkerOptions()
        markerOptions.position(currentLatLng)
        markerOptions.title(markerTitle)
        markerOptions.snippet(markerSnippet)
        markerOptions.draggable(true)
        currentMarker = mMap!!.addMarker(markerOptions)
        val cameraUpdate = CameraUpdateFactory.newLatLng(currentLatLng)
        mMap!!.moveCamera(cameraUpdate)
    }

    fun setDefaultLocation() {
        //현재위치정보를 가져올수없을때 기본위치를 설정 해당위치에 마커추가
        val DEFAULT_LOCATION = LatLng(37.56, 126.97)//기본위치(서울)
        val markerTitle = "위치정보 가져올 수 없음"
        val markerSnippet = "위치 퍼미션과 GPS 활성 요부 확인하세요"
        if (currentMarker != null) currentMarker!!.remove()
        val markerOptions = MarkerOptions()
        markerOptions.position(DEFAULT_LOCATION)
        markerOptions.title(markerTitle)
        markerOptions.snippet(markerSnippet)
        markerOptions.draggable(true)
        markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE))
        currentMarker = mMap!!.addMarker(markerOptions)
        val cameraUpdate = CameraUpdateFactory.newLatLngZoom(DEFAULT_LOCATION, 15f)
        mMap!!.moveCamera(cameraUpdate) //카메라이동
    }

    //위치정보를위해 런타임 퍼미션 처리을 위한 메소드들
    //두가지 퍼미션이 모두 허용되어있는경우 true를 반환 현재위치 정보를가져오기전에 퍼미션체크수행
    private fun checkPermission(): Boolean {
        val hasFineLocationPermission = ContextCompat.checkSelfPermission(
            appMainActivity,
            Manifest.permission.ACCESS_FINE_LOCATION
        )
        val hasCoarseLocationPermission = ContextCompat.checkSelfPermission(
            appMainActivity,
            Manifest.permission.ACCESS_COARSE_LOCATION
        )
        return hasFineLocationPermission == PackageManager.PERMISSION_GRANTED &&
                hasCoarseLocationPermission == PackageManager.PERMISSION_GRANTED
    }

    /*
    * ActivityCompat.requestPermissions를 사용한 퍼미션 요청의 결과를 리턴받는 메소드입니다.
    */
    override fun onRequestPermissionsResult(
        permsRequestCode: Int,
        permissions: Array<String>,
        grandResults: IntArray
    ) {
        super.onRequestPermissionsResult(permsRequestCode, permissions, grandResults)

        if (permsRequestCode == PERMISSIONS_REQUEST_CODE && grandResults.size == requiredPermissions.size) {
            var check_result = true

            for (result: Int in grandResults) {
                if (result != PackageManager.PERMISSION_GRANTED) {
                    check_result = false
                    break
                }
            }

            if (check_result) {
                startLocationUpdates()
            } else {
                if (ActivityCompat.shouldShowRequestPermissionRationale(
                        requireActivity(),
                        requiredPermissions[0]
                    )
                    || ActivityCompat.shouldShowRequestPermissionRationale(
                        requireActivity(),
                        requiredPermissions[1]
                    )
                ) {
                    Snackbar.make(
                        requireView(), "퍼미션이 거부되었습니다. 퍼미션을 허용해주세요.",
                        Snackbar.LENGTH_INDEFINITE
                    ).setAction("확인") { checkPermissions() }.show()
                } else {
                    Snackbar.make(
                        requireView(), "퍼미션이 거부되었습니다.  퍼미션을 허용해야 합니다.",
                        Snackbar.LENGTH_INDEFINITE
                    ).setAction("확인") { checkPermissions() }.show()
                }
            }
        }
    }

    //여기부터는 GPS 활성화를 위한 메소드들
    private fun showDialogForLocationServiceSetting() {
        val builder = AlertDialog.Builder(appMainActivity)
        builder.setTitle("위치 서비스 비활성화")
        builder.setMessage(
            "앱을 사용하기 위해서는 위치 서비스가 필요합니다.\n"
                    + "위치 설정을 수정하실래요?"
        )
        builder.setCancelable(true)
        builder.setPositiveButton("설정") { _, _ ->
            val callGPSSettingIntent =
                Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
            startActivityForResult(
                callGPSSettingIntent,
                GPS_ENABLE_REQUEST_CODE
            )
        }
        builder.setNegativeButton(
            "취소"
        ) { dialog, id -> dialog.cancel() }
        builder.create().show()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            GPS_ENABLE_REQUEST_CODE ->
                //사용자가 GPS 활성 시켰는지 검사
                if (checkLocationServicesStatus()) {
                    if (checkLocationServicesStatus()) {
                        Log.d(TAG, "onActivityResult : GPS 활성화 되있음")
                        needRequest = true
                        return
                    }
                }
        }
    }

    companion object {
        private const val TAG = "googlemap_example"
        private const val GPS_ENABLE_REQUEST_CODE = 2001

        // onRequestPermissionsResult에서 수신된 결과에서 ActivityCompat.requestPermissions를 사용한 퍼미션 요청을 구별하기 위해 사용됩니다.
        private const val PERMISSIONS_REQUEST_CODE = 100
    }

    override fun onPlacesFailure(e: PlacesException?) {
    }

    override fun onPlacesStart() {
    }

    override fun onPlacesSuccess(places: MutableList<Place>?) {
        lifecycleScope.launch(Dispatchers.Main) {
            binding.progressBar.visibility = View.GONE
            //디폴트 위치, Seoul
            var descripter: BitmapDescriptor? = null

            if (currentflag == 1) {
                var bitmapDrawable: BitmapDrawable = context?.getDrawable(R.drawable.restaurant) as BitmapDrawable
                val scaleBitmap = Bitmap.createScaledBitmap(bitmapDrawable.bitmap, 60, 60, false)
                descripter = BitmapDescriptorFactory.fromBitmap(scaleBitmap)
            } else if (currentflag == 2) {
                var bitmapDrawable: BitmapDrawable =
                    context?.getDrawable(R.drawable.cafe) as BitmapDrawable
                val scaleBitmap = Bitmap.createScaledBitmap(bitmapDrawable.bitmap, 60, 60, false)
                descripter = BitmapDescriptorFactory.fromBitmap(scaleBitmap)
            }
//            requireActivity().runOnUiThread {
            if (places != null) {
                for (place in places) {
                    val latLng = LatLng(
                        place.latitude, place.longitude
                    )
                    val markerSnippet = getCurrentAddress(latLng)
                    val markerOptions = MarkerOptions()
                    markerOptions.position(latLng)
                    markerOptions.icon(descripter)
                    markerOptions.title(place.name)
                    markerOptions.snippet(markerSnippet)
                    markerOptions.alpha(0.5f)
                    val item = mMap!!.addMarker(markerOptions)
                    previousMarker?.add(item as Marker)
                }
            }
            //중복 마커 제거
            val hashSet = HashSet<Marker>()
            hashSet.addAll(previousMarker!!)
            previousMarker?.clear()
            previousMarker?.addAll(hashSet)
//            }
        }
    }

    override fun onPlacesFinished() {
    }

    private fun showRestInformation(location: LatLng, location2: LatLng) {
        currentflag = 1
        binding.progressBar.visibility = View.VISIBLE

        if (searchflag == 1) {
            mMap!!.clear() //지도 클리어
            if (previousMarker != null)
                previousMarker?.clear()//지역정보 마커 클리어
            NRPlaces.Builder()
                .listener(this)
                .key(BuildConfig.googleMap_Key)
                .latlng(location.latitude, location.longitude) //현재 위치
                .radius(500) //500 미터 내에서 검색
                .type(PlaceType.RESTAURANT) //음식점
                .build()
                .execute()
            if (circle1KM == null) {
                circle1KM = CircleOptions().center(location) // 원점
                    .radius(500.0) // 반지름 단위 : m
                    .strokeWidth(1.0f) // 선너비 0f : 선없음
                    .fillColor(Color.parseColor("#80C7EDE8")) // 배경색 (회색)
                circle = mMap!!.addCircle(circle1KM!!)
            } else {
                circle?.remove() // 반경삭제
                circle1KM?.center(location)
                circle1KM?.fillColor(Color.parseColor("#80C7EDE8")) // 배경색 (회색)
                circle = mMap!!.addCircle(circle1KM!!)
            }
        }
        if (searchflag == 2) {
            mMap!!.clear() //지도 클리어
            if (previousMarker != null)
                previousMarker?.clear()//지역정보 마커 클리어
            NRPlaces.Builder()
                .listener(this)
                .key(BuildConfig.googleMap_Key)
                .latlng(location2.latitude, location2.longitude) //현재 위치
                .radius(500) //500 미터 내에서 검색
                .type(PlaceType.RESTAURANT) //음식점
                .build()
                .execute()
            if (circle1KM == null) {
                circle1KM = CircleOptions().center(location2) // 원점
                    .radius(500.0) // 반지름 단위 : m
                    .strokeWidth(1.0f) // 선너비 0f : 선없음
                    .fillColor(Color.parseColor("#80C7EDE8")) // 배경색 (회색)
                circle = mMap!!.addCircle(circle1KM!!)
            } else {
                circle?.remove() // 반경삭제
                circle1KM?.center(location2)
                circle1KM?.fillColor(Color.parseColor("#80C7EDE8")) // 배경색 (회색)
                circle = mMap!!.addCircle(circle1KM!!)
            }
        }

    }

    private fun showCafeInformation(location: LatLng, location2: LatLng) {
        currentflag = 2
        binding.progressBar.visibility = View.VISIBLE

        if (searchflag == 1) {
            mMap!!.clear() //지도 클리어
            if (previousMarker != null)
                previousMarker?.clear()//지역정보 마커 클리어
            NRPlaces.Builder()
                .listener(this)
                .key(BuildConfig.googleMap_Key)
                .latlng(location.latitude, location.longitude) //현재 위치
                .radius(500) //500 미터 내에서 검색
                .type(PlaceType.CAFE) //음식점
                .build()
                .execute()
            if (circle1KM == null) {
                circle1KM = CircleOptions().center(location) // 원점
                    .radius(500.0) // 반지름 단위 : m
                    .strokeWidth(1.0f) // 선너비 0f : 선없음
                    .fillColor(Color.parseColor("#80C7EDE8")) // 배경색 (회색)
                circle = mMap!!.addCircle(circle1KM!!)
            } else {
                circle?.remove() // 반경삭제
                circle1KM?.center(location)
                circle1KM?.fillColor(Color.parseColor("#80C7EDE8")) // 배경색 (회색)
                circle = mMap!!.addCircle(circle1KM!!)
            }
        }
        if (searchflag == 2) {
            mMap!!.clear() //지도 클리어
            if (previousMarker != null)
                previousMarker?.clear()//지역정보 마커 클리어
            NRPlaces.Builder()
                .listener(this)
                .key(BuildConfig.googleMap_Key)
                .latlng(location2.latitude, location2.longitude) //현재 위치
                .radius(500) //500 미터 내에서 검색
                .type(PlaceType.CAFE) //카페
                .build()
                .execute()
            if (circle1KM == null) {
                circle1KM = CircleOptions().center(location2) // 원점
                    .radius(500.0) // 반지름 단위 : m
                    .strokeWidth(1.0f) // 선너비 0f : 선없음
                    .fillColor(Color.parseColor("#80C7EDE8")) // 배경색 (회색)
                circle = mMap!!.addCircle(circle1KM!!)
            } else {
                circle?.remove() // 반경삭제
                circle1KM?.center(location2)
                circle1KM?.fillColor(Color.parseColor("#80C7EDE8")) // 배경색 (회색)
                circle = mMap!!.addCircle(circle1KM!!)
            }
        }
    }
}