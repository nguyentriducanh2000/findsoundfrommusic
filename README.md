# Findsoundfrommusic
### Giới thiệu
Ứng dụng cho phép người dùng ghi âm 1 đoạn nhạc, từ đó tìm ra được tên và thông tin của bài nhạc đó.

Src drive- chia sẻ thuật toán và thiết lập server: https://drive.google.com/drive/folders/1KccK8VL3kUQkQzbiZTL4VfH9zsZBJ17E?usp=sharing
### I.Thiết lập server
- Sử dụng server miễn phí của GCP. Thuê 1 máy chủ phù hợp( server test có thông số: e2-8 vCPUs, 8 GB memory, ubuntu 20.04 LTS)
- Cài đặt các thư viện và tiện ích cần thiết trên server.
- Dowload các file từ drive chia sẻ về máy ảo
- Khởi chạy server: `python server.py`

### II.Hướng dẫn cài đặt
- Download project từ github về máy. Sử dụng phần mềm Android Studio (phiên bản >=3.) để mở project.
- (Tùy chọn)Chỉnh sửa ip ở các file `findsoundfrommusic/app/src/main/res/xml/network_security_config.xml` và `findsoundfrommusic/app/src/main/java/com/example/find/MainActivity.java` để sử dụng server thuê, hoặc giữ nguyên để sử dụng server test- hạn dùng đến 15-2-2020
- Đóng gói thành app .apk để sử dụng.
### III.Hướng dẫn sử dụng
- Khởi chạy app
- Cấp quyền permimssion cho app.
- Nhấn vào biểu tượng để bắt đầu
- Bắt đầu ghi âm
- Chờ kết quả trả về
- Kết quả hiển thị

