# 使用说明
***
## 这是什么？
此app会将qq的发送数据和接收数据以http post的方式发送到自定义地址
***
## 权限说明
读取和存储权限，用于保存用户设置的自定义地址
## 发送格式
POST: address/send?seq=%s&command=%s&uin=%s
data: part2的字节流

POST: address/receive?seq=%s&command=%s&uin=%s
data: part2的字节流
