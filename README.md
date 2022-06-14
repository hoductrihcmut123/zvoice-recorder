# zvoice-recorder



## Requirements

Ứng dụng record voice với các tính năng sau:
 + Quản lý danh sách các file đã record, cho nghe lại
 + Cho record 1 voice mới, home app hay khoá màn hình đều phải record được
 + Record liên tục, tức dù đang record mà app bị crash hay hết pin restart thì file đang record được save thành công ko mất
 + Dùng sqlite lưu trữ data, không dùng thư viện mapping sqlite
 + Màn hình nghe lại có noti để có thể chạy background như các app nghe nhạc 
 + (Optional) Cho phép upload lên drive dùng HTTP REST API
 
 Hạn chế dùng library bên ngoài trừ các lib support, lib google-service

