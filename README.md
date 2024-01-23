<h1>
 랜선 소개팅 앱 Hipeople👩‍❤️‍👨
</h1>

<p align="center">  
 
 ![Untitled](https://github.com/AnMyungwoo94/Hipeople_App/assets/126849689/d6a6ff06-4a8a-4aec-a413-cb5e10accd4e)

</p> 

### <img src="https://github.com/AnMyungwoo94/BeautyIdea_Shopping_App/assets/126849689/d0ba6eb9-f5c2-4839-ad30-6f8bf65c7452" alt="구글 플레이 이미지" width="30" height="" style="float:left"> [ 앱 다운로드 링크 ](https://play.google.com/store/apps/details?id=com.myungwoo.hipeople)

## 1. 소개
"Hipeople"는 "Hi(안녕) 사람들"이라는 뜻이며, 사람들간의 소통을 도와주는 소개팅, 친목도모 앱입니다. 타인과 채팅 및 익명 커뮤니티에서 자유롭게 대화를 나눌 수 있습니다. 저희앱은 비대면으로도 친밀감을 형성할 수 있는 기회를 제공하기 위해 개발되었습니다.

- 회원가입시 나이, 성별, 지역 등 기본 정보를 입력 받습니다.
- 이성 추천 카드로 마음에 드는 이성을 선택할 수 있습니다.
- 이성과 채팅, 알림 기능이 지원됩니다.
- 구글지도를 활용하여 주변 맛집, 카페 등을 찾을 수 있습니다.
- 익명 커뮤니티로 본인의 고민거리 등을 자유롭게 공유할 수 있습니다.

## 2. 최근 업데이트
- 23/09/01일 Google play 앱 배포하기
- 23/08/27일 이성 좋아요 내역 확인하기
- 23/08/19일 채팅방 알림 구현

## 3. DB 및 체크리스트
[hipeople DB.docx](https://github.com/AnMyungwoo94/Hipeople_App/files/13539174/hipeople.DB.docx)

[hipeople 체크리스트_안명우.docx](https://github.com/AnMyungwoo94/Hipeople_App/files/13539175/hipeople._.docx)

## 4. 기능소개 
![Frame 4 (1)](https://github.com/AnMyungwoo94/Hipeople_App/assets/126849689/0fd19a08-3b8e-47f2-a9d7-0215e1782e23)


### 로그인, 회원가입

|스플래시, 로그인|회원가입, 프로필 정보|정보 수정|
|:-----:|:-----:|:-----:|
|<img width="250" src="https://github.com/AnMyungwoo94/Hipeople_App/assets/126849689/b1899b0f-5a0c-41ce-934a-0bd9e96dc100.gif">|<img width="250" src="https://github.com/AnMyungwoo94/Hipeople_App/assets/126849689/5b65d299-3802-4b32-bfb3-6ec4194f30ed.gif">|<img width="250" src="https://github.com/AnMyungwoo94/Hipeople_App/assets/126849689/27519ea8-a0fe-4ca2-81bc-303dfbf671ee.gif">|

### 이성추천

|이성추천 카드|좋아요|채팅이동|
|:-----:|:-----:|:-----:|
|<img width="250" src="https://github.com/AnMyungwoo94/Hipeople_App/assets/126849689/559b67c3-32fe-4ca1-a730-5a0e50478dbf.gif">|<img width="250" src="https://github.com/AnMyungwoo94/Hipeople_App/assets/126849689/2d3ac379-e904-49c7-bc02-5a82013ad9cf.gif">|<img width="250" src="https://github.com/AnMyungwoo94/Hipeople_App/assets/126849689/d9888fb6-49c5-4953-a1ed-828eda2b06d9.gif" />|

- 소개팅앱의 포인트가 되는 중요한 화면이며, 사용자들이 직관적이면서도 재미있게 이성을 선택할 수 있게 만들었습니다.
- 글램, 위피, 틴더 등 시중에 나와있는 다양한 소개팅앱을 사용해보며 고민하였습니다.
- **CardStackView 외부 라이브러리 사용한 이유**는 RecyclerView와 CardView의 조합으로도 충분히 구현이 가능했지만 외부 라이브러리의 사용 경험을 늘려보고 싶었습니다.

### 채팅

|채팅방|채팅 목록|알림|
|:-----:|:-----:|:-----:|
|<img width="250" src="https://github.com/AnMyungwoo94/Hipeople_App/assets/126849689/28710ea2-e080-471b-bdba-61847d96727f.gif">|<img width="250" src="https://github.com/AnMyungwoo94/Hipeople_App/assets/126849689/b44fe779-819d-4386-8882-2875cf8eeceb.gif">|<img width="250" src="https://github.com/AnMyungwoo94/Hipeople_App/assets/126849689/894e2828-d499-48cb-9736-b86fc05762b9.gif" />|

- 알림 Firebase cloud messaging의 Token값 사용
- **Firebase cloud messaging를 사용한 이유**는 OneSignal과 같은 다른 알림 서비스를 고려해보았지만, 현재 사용 중인 데이터베이스 서비스가 파이어베이스인 점, 동일한 플랫폼 내에서 알림 기능을 통합하여 사용할 수 있는 편의성, 그리고 로그인 시 즉시 사용 가능한 토큰 값을 얻을 수 있는 점을 고려하여, 파이어베이스를 알림 서비스로 선택하게 되었습니다.
- 개인마다 고유한 토큰 값을 가져오는 과정에서 **모든 사용자의 토큰 값이 항상 동일한 문제가 발생**했습니다. 이 문제는 동일한 에뮬레이터에서 여러 회원가입을 수행하여 발생했으며, 토큰 값은 기기별로 고유하게 제공되는 것을 인지하고 이 문제를 해결했습니다.
  
### 구글맵

|현재 내 위치|카페, 맛집 찾기|
|:-----:|:-----:|
|<img width="250" src="https://github.com/AnMyungwoo94/Hipeople_App/assets/126849689/ebb6fafd-832d-4387-8533-469c80e22c78.gif">|<img width="250" src="https://github.com/AnMyungwoo94/Hipeople_App/assets/126849689/e2427f1c-8367-4b25-ba66-cc3ecfd39393.gif">|

- **구글지도를 사용한 이유**는 국내에서는 네이버지도, 카카오맵, 티맵과 같은 지도 서비스가 매우 유용하지만, 원래의 기획 의도가 국내뿐만 아니라 해외에서도 사용 가능한 앱을 개발하는 것이었기 때문에, 국내와 해외 모두에서 널리 사용될 수 있는 구글 지도를 선택했습니다.

### 익명 커뮤니티

|게시글 작성|게시글 좋아요, 댓글|
|:-----:|:-----:|
|<img width="250" src="https://github.com/AnMyungwoo94/Hipeople_App/assets/126849689/97faf90d-acc9-41a4-87d3-39e034ce0d60.gif">|<img width="250" src="https://github.com/AnMyungwoo94/Hipeople_App/assets/126849689/aca2362c-6ddc-46e6-81ba-4a806960ba4c.gif">|


## 4. 기술스택
- Google places API
- Coroutines
- Retrofit
- Glide
- FireBase DB : realtime database, authentication, storage, FCM

## 5. 프로젝트 마무리 하며 느낀 점
팀 프로젝트 작업 기간 : 3월 20일 ~ 4월 10일
처음 진행해 본 팀 프로젝트였다. 시간에 쫒겨 가장 편하고 빠른 방법으로 프로젝트를 진행하고자 했고,
여러가지 시행착오를 겪으면서 이를 통해 많은 것을 배우고 얻었다.

< 좋았던 점 >
1. 자유 주제로 팀원들과 같이 이야기하며 여러가지 주제를 놓고 의논하였다.
여러가지 주제에서 하나를 선정하기까지 각자의 생각을 나누며, 다양한 의견을 들어보고 말할 수 있어서 좋았다.

2. 여러가지 기술 학습
주제가 정해지면서 맡은 기술을 구현하기 위해 여러 정보를 찾아보았다.
이성추천 카드를 만들기 위해 어떻게 하면 사용자가 재미있게 할 수 있을지 생각해보고, 리사이클러뷰 + 카드뷰를 활용해서
직접 만들어볼 수 있었지만 더 다양한 기능을 제공하는 카드스텍뷰 라이브러리를 찾아서 적용시켜 보았다.
이 때 카드를 활용한 라이브러리도 정말 많다는 것을 깨달았고, 라이브러리는 언제 중단될지 모르니 사용시 대체할 수 있는
플랜B 를 찾아보고 적용하는게 좋겠다 라는 생각이 들었다.

3. 의사소통 능력
구현을 하면서 막혔던 부분이 있었다. 혼자서 해결해 보고 싶었지만 팀프로젝트의 기간은 정해져 있었기에 욕심을 버리고
팀원들에게 도움을 구하였다. 내가 구현하고 있는 코드였기에 해당 코드에 대해 설명해보면서 내 머릿속에서도 정리해볼 수 있었다.
또한, 도움을 구할때는 내가 해보았던 부분을 정확하게 이야기를 하고 오류가 나는 부분이 정확이 어디인지 알려주어야
다른 팀원들에게 빠르게 도움을 받을 수 있다는 것도 깨달을 수 있었다.

4. 많은 오류 경험
정말 많은 오류를 경험하고 해결하면서 중복되는 오류는 더 이상 구글링해서 찾아 보지 않아도 되어 시간을 줄일 수 있었다.

## 6. 개선사항
1. 깃허브 협업경험을 하지 못하여 아쉬움이 남는다.
깃허브를 통해 각자의 브랜치를 생성, 코드리뷰, PR, 네이밍 컨벤션 정하기 등 팀원들끼리 같이 하지 못하였다.
아무래도 프로젝트 파일의 크기가 작아 알집으로 묶어서 서로 공유해도 충분했다고 생각했다.
다음 팀 프로젝트 때는 꼭 이러한 부분을 개선해야겠다.

2. 구현하고자 했던 페이지를 다 만들지 못했던 점
여러가지를 구현하고 싶었고, 그 대부분의 구현을 끝냈지만 그래도 구현이 남은 부분이 있다.
이 부분을 내가 따로 구현할 예정이다.

- 앱 배포하기 (9월 완료)
- 카드뷰 좋아요 내역 확인하기 (8/27 완료)
- 채팅방 알림 구현하기 (8/19 완료)
- 단체 채팅방 구현하기
- MBTI 유형별 검색기능 구현하기
- 채팅시 비속어, 성적단어 거르기
- 채팅시 이미지 첨부 가능하게 하기
  
## 7. 맡은 역할
- 전체 UI 디자인 작업
- 채팅관련 (채팅창, 채팅목록,알림) 화면 구현
- 회원정보 관리 화면 구현
- 이성추천 카드 화면 구현
