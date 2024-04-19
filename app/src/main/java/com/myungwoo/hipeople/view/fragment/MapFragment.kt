package com.myungwoo.hipeople.view.fragment

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
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
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
import com.myungwoo.hipeople.BuildConfig
import com.myungwoo.hipeople.R
import com.myungwoo.hipeople.adapter.CustomAdapter
import com.myungwoo.hipeople.databinding.FragmentMapBinding
import com.myungwoo.hipeople.view.activity.AppMainActivity
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
    private var searchLATLNG = LatLng(0.0, 0.0)
    private var mMap: GoogleMap? = null
    private var currentMarker: Marker? = null
    private var needRequest = false
    private var previousMarker: MutableList<Marker>? = null
    private var currentFlag = 0
    private var searchFlag = 0
    private var circle: Circle? = null
    private var circle1KM: CircleOptions? = null
    private var mCurrentLocation: Location? = null
    private var currentPosition: LatLng? = null
    private var mFusedLocationClient: FusedLocationProviderClient? = null
    private var locationRequest: LocationRequest? = null
    private var location: Location? = null
    private var mLayout: View? = null

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

        requireActivity().window.setFlags(
            WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON,
            WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
        )

        if (isLocationPermissionRequired()) {
            checkPermissions()
        }

        setupLocationUpdates()
        binding.btnRestaurant.setOnClickListener {
            Toast.makeText(context, "조금만 기다려주세요! 곧 지도를 불러오겠습니다^^", Toast.LENGTH_SHORT).show()
            if (currentPosition != null) {
                lifecycleScope.launch(Dispatchers.Default) {
                    withContext(Dispatchers.Main) {
                        showRestInformation(currentPosition!!, searchLATLNG)
                    }
                }
            } else {
                Toast.makeText(context, "위치 정보가 없습니다. 위치정보에서 Hipeople앱을 허용해주세요", Toast.LENGTH_SHORT)
                    .show()
                checkPermissions()
            }
        }

        binding.btnCafe.setOnClickListener {
            Toast.makeText(context, "조금만 기다려주세요! 곧 지도를 불러오겠습니다^^", Toast.LENGTH_SHORT).show()
            if (currentPosition != null) {
                lifecycleScope.launch(Dispatchers.Default) {
                    withContext(Dispatchers.Main) {
                        showCafeInformation(currentPosition!!, searchLATLNG)
                    }
                }
            } else {
                Toast.makeText(
                    context,
                    "위치 정보가 없습니다. 위치정보에서 Hipeople앱을 허용해주세요 ",
                    Toast.LENGTH_SHORT
                ).show()
                checkPermissions()
            }
        }
    }

    private fun isLocationPermissionRequired(): Boolean {
        val viewPager = requireActivity().findViewById<ViewPager2>(R.id.viewPager2)
        val currentFragment =
            (viewPager.adapter as? CustomAdapter)?.createFragment(viewPager.currentItem)
        return currentFragment is MapFragment
    }

    private fun checkPermissions() {
        val fineLocationPermissionGranted = ActivityCompat.checkSelfPermission(
            requireContext(), Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        val coarseLocationPermissionGranted = ActivityCompat.checkSelfPermission(
            requireContext(), Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        if (fineLocationPermissionGranted && coarseLocationPermissionGranted) {
            startLocationUpdates()
            return
        }

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
        locationRequest = LocationRequest()
            .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
            .setInterval(120000)
            .setFastestInterval(110000)

        val builder = LocationSettingsRequest.Builder()
        builder.addLocationRequest(locationRequest!!)

        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity())

        val mapFragment = childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment?
        mapFragment!!.getMapAsync(this)
        previousMarker = ArrayList()
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(appMainActivity)
        updateLocation()

        val autocompleteFragment =
            childFragmentManager.findFragmentById(R.id.autocomplete_fragment)
                    as AutocompleteSupportFragment
        autocompleteFragment.setPlaceFields(
            listOf(
                com.google.android.libraries.places.api.model.Place.Field.ID,
                com.google.android.libraries.places.api.model.Place.Field.NAME,
                com.google.android.libraries.places.api.model.Place.Field.LAT_LNG
            )
        )

        autocompleteFragment.setOnPlaceSelectedListener(object : PlaceSelectionListener {
            override fun onPlaceSelected(p0: com.google.android.libraries.places.api.model.Place) {
                val location = Location("")
                location.latitude = p0.latLng.latitude
                location.longitude = p0.latLng.longitude

                searchLATLNG = LatLng(location.latitude, location.longitude)
                val markerOptions = MarkerOptions().position(searchLATLNG)
                val cameraPosition =
                    CameraPosition.Builder().target(searchLATLNG).zoom(15.0f).build()
                mMap?.clear()
                mMap?.addMarker(markerOptions)
                mMap?.moveCamera(CameraUpdateFactory.newCameraPosition(cameraPosition))
                searchFlag = 2
            }

            override fun onError(status: Status) {
                Log.e(TAG, "An error occurred: ${status.statusMessage}")
            }
        })
        setDefaultLocation()
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

    private var locationCallback: LocationCallback = object : LocationCallback() {
        override fun onLocationResult(locationResult: LocationResult) {
            super.onLocationResult(locationResult)
            val locationList = locationResult.locations
            if (locationList.size > 0) {
                location = locationList[locationList.size - 1]
                currentPosition = LatLng(location!!.latitude, location!!.longitude)
                val markerTitle = getCurrentAddress(currentPosition!!)
                val markerSnippet =
                    "위도:" + location!!.latitude.toString() + " 경도:" + location!!.longitude.toString()
                Log.d(TAG, "onLocationResult : $markerSnippet")
                searchFlag = 1

                setCurrentLocation(location, markerTitle, markerSnippet)
                mCurrentLocation = location
            }
        }
    }

    private fun startLocationUpdates() {
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(appMainActivity)
        if (!checkLocationServicesStatus()) {
            Log.d(TAG, "startLocationUpdates : call showDialogForLocationServiceSetting")
            showDialogForLocationServiceSetting()
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

    private fun getCurrentAddress(latlng: LatLng): String {
        val geocoder = Geocoder(appMainActivity, Locale.getDefault())
        val addresses: List<Address>?
        try {
            addresses = geocoder.getFromLocation(
                latlng.latitude,
                latlng.longitude,
                1
            )
        } catch (ioException: IOException) {
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

    private fun setCurrentLocation(
        location: Location?,
        markerTitle: String?,
        markerSnippet: String?
    ) {
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

    private fun setDefaultLocation() {
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
        private const val PERMISSIONS_REQUEST_CODE = 100
    }

    override fun onPlacesFailure(e: PlacesException?) {}
    override fun onPlacesStart() {}
    override fun onPlacesSuccess(places: MutableList<Place>?) {
        lifecycleScope.launch(Dispatchers.Main) {
            binding.progressBar.visibility = View.GONE
            var descripter: BitmapDescriptor? = null

            if (currentFlag == 1) {
                var bitmapDrawable: BitmapDrawable =
                    context?.getDrawable(R.drawable.restaurant) as BitmapDrawable
                val scaleBitmap = Bitmap.createScaledBitmap(bitmapDrawable.bitmap, 60, 60, false)
                descripter = BitmapDescriptorFactory.fromBitmap(scaleBitmap)
            } else if (currentFlag == 2) {
                var bitmapDrawable: BitmapDrawable =
                    context?.getDrawable(R.drawable.cafe) as BitmapDrawable
                val scaleBitmap = Bitmap.createScaledBitmap(bitmapDrawable.bitmap, 60, 60, false)
                descripter = BitmapDescriptorFactory.fromBitmap(scaleBitmap)
            }
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
            val hashSet = HashSet<Marker>()
            hashSet.addAll(previousMarker!!)
            previousMarker?.clear()
            previousMarker?.addAll(hashSet)
        }
    }

    override fun onPlacesFinished() {}

    private fun showRestInformation(location: LatLng, location2: LatLng) {
        currentFlag = 1
        binding.progressBar.visibility = View.VISIBLE

        if (searchFlag == 1) {
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
        if (searchFlag == 2) {
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
        currentFlag = 2
        binding.progressBar.visibility = View.VISIBLE

        if (searchFlag == 1) {
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
        if (searchFlag == 2) {
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