![image](https://github.com/hoductrihcmut123/zvoice-recorder/assets/76983358/f6d37ab5-38d3-423f-92fd-19d848997110)# zvoice-recorder
## Android, Kotlin



## Requirements

Ứng dụng record voice với các tính năng sau:
 + Quản lý danh sách các file đã record, cho nghe lại
 + Cho record 1 voice mới, home app hay khoá màn hình đều phải record được
 + Record liên tục, tức dù đang record mà app bị crash hay hết pin restart thì file đang record được save thành công ko mất
 + Dùng sqlite lưu trữ data, không dùng thư viện mapping sqlite
 + Màn hình nghe lại có noti để có thể chạy background như các app nghe nhạc 
 + (Optional) Cho phép upload lên drive dùng HTTP REST API
 
 Hạn chế dùng library bên ngoài trừ các lib support, lib google-service


## Record một voice mới

 <img src="https://github.com/hoductrihcmut123/zvoice-recorder/assets/76983358/3a83eff2-2de8-4e6b-902e-06bf95ff2f98" width="175">
 <img src="https://github.com/hoductrihcmut123/zvoice-recorder/assets/76983358/02f727e6-0cb6-4660-8039-9a5695ee0d59" width="175">
 <img src="https://github.com/hoductrihcmut123/zvoice-recorder/assets/76983358/932794b2-028c-4700-b075-56bbc7d631f8" width="175">
 <img src="https://github.com/hoductrihcmut123/zvoice-recorder/assets/76983358/00bab9d6-e8c4-4ce7-8c0d-76273e2b599e" width="175">
 <img src="https://github.com/hoductrihcmut123/zvoice-recorder/assets/76983358/04fa45fd-3726-4ab0-a64c-c8b31a7f986e" width="175">
 <img src="https://github.com/hoductrihcmut123/zvoice-recorder/assets/76983358/a397c6f9-d70e-4fe0-925d-50243644c38a" width="175">
 <img src="https://github.com/hoductrihcmut123/zvoice-recorder/assets/76983358/a574a1e6-22a0-4384-b8e6-a370926765c3" width="175">


## Record liên tục 

 <img src="https://github.com/hoductrihcmut123/zvoice-recorder/assets/76983358/229405fe-3b7f-492d-aaad-075b09cee903" width="175">
 <img src="https://github.com/hoductrihcmut123/zvoice-recorder/assets/76983358/c742f423-c077-4e86-8f61-f6a75b18bced" width="175">
 <img src="https://github.com/hoductrihcmut123/zvoice-recorder/assets/76983358/ec94857f-b860-4d50-9497-50fa38c40bd3" width="175">
 <img src="https://github.com/hoductrihcmut123/zvoice-recorder/assets/76983358/f4568d0f-fa56-4edd-8e1d-cb61862f6c4f" width="175">


## Quản lý danh sách các file

** Đổi tên file:

 <img src="https://github.com/hoductrihcmut123/zvoice-recorder/assets/76983358/2c24dbb2-5206-4adc-85f2-74ec7b37f31d" width="175">
 <img src="https://github.com/hoductrihcmut123/zvoice-recorder/assets/76983358/745d12c8-5833-49f0-b1d8-ce2ad51663db" width="175">
 <img src="https://github.com/hoductrihcmut123/zvoice-recorder/assets/76983358/7b7066a4-6477-44da-b60c-a0ed4e10e282" width="175">
 
** Search theo tên:

 <img src="https://github.com/hoductrihcmut123/zvoice-recorder/assets/76983358/6b8b0788-36ad-47b1-8ec4-7d062491cdda" width="175">
 
** Xóa 1 file:

<img src="https://github.com/hoductrihcmut123/zvoice-recorder/assets/76983358/06187c57-a009-463c-a08f-93ebb786b22a" width="175">
<img src="https://github.com/hoductrihcmut123/zvoice-recorder/assets/76983358/09a484c0-de01-4e28-ae0d-2e4d31cd9a90" width="175">

** Xóa nhiều file:

 <img src="https://github.com/hoductrihcmut123/zvoice-recorder/assets/76983358/b43ec66c-b777-45f1-8d68-815f58b6cdee" width="175">
 <img src="https://github.com/hoductrihcmut123/zvoice-recorder/assets/76983358/6a3c4266-5aeb-4ae4-bda9-7a272992938e" width="175">
 <img src="https://github.com/hoductrihcmut123/zvoice-recorder/assets/76983358/af2d7358-cf4b-4d1f-95c5-ff5ffee1e915" width="175">
 <img src="https://github.com/hoductrihcmut123/zvoice-recorder/assets/76983358/0152cdf9-d87c-4088-8eb4-93211283e9fc" width="175">
 

## Nghe lại các file đã record

 <img src="https://github.com/hoductrihcmut123/zvoice-recorder/assets/76983358/0445c791-bba4-419a-bc22-e688efcbc119" width="175">
 <img src="https://github.com/hoductrihcmut123/zvoice-recorder/assets/76983358/a667c593-5886-48ba-9c2f-5d221f6daed6" width="175">
 <img src="https://github.com/hoductrihcmut123/zvoice-recorder/assets/76983358/dc74d71b-1cd2-4897-8f85-ac6972870a41" width="175">
 <img src="https://github.com/hoductrihcmut123/zvoice-recorder/assets/76983358/a45bab4d-428f-4574-b74f-da5601823684" width="175">
 

## Upload lên Drive

** Upload 1 file

 <img src="https://github.com/hoductrihcmut123/zvoice-recorder/assets/76983358/41dc8914-db36-4f7f-b553-d95e34bb685c" width="175">
 <img src="https://github.com/hoductrihcmut123/zvoice-recorder/assets/76983358/9eb02421-4e4e-40d2-80d4-d1a1b09abd6b" width="175">
 <img src="https://github.com/hoductrihcmut123/zvoice-recorder/assets/76983358/6f55f30f-c6cb-4c19-a6d9-35655312c88e" width="175">

** Upload nhiều file

 <img src="https://github.com/hoductrihcmut123/zvoice-recorder/assets/76983358/b4bb75c6-b9fb-4017-ba8f-1736955e279c" width="175">
 <img src="https://github.com/hoductrihcmut123/zvoice-recorder/assets/76983358/54fdb0e9-a905-4467-97d6-d52b43c1d05a" width="175">
 <img src="https://github.com/hoductrihcmut123/zvoice-recorder/assets/76983358/17e700c0-d0b5-4b16-b696-daa8c7407077" width="175">
 <img src="https://github.com/hoductrihcmut123/zvoice-recorder/assets/76983358/af169331-e822-4c28-9ca8-fd82bcacad68" width="175">
 <img src="https://github.com/hoductrihcmut123/zvoice-recorder/assets/76983358/7ccdbd6e-fe7f-4f2b-a77a-80ef3b8f13a4" width="175">
 <img src="https://github.com/hoductrihcmut123/zvoice-recorder/assets/76983358/3d395c93-b757-4553-af8a-b18e97b33755" width="175">

