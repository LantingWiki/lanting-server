## ClipArchive 添加文章工作流

1. 用户发送一条链接url给公众号. 这会触发 (用户的信息和用户的聊天内容, 微信会转发到我们设定好的接口):
    - 这条链接会被保存, 并生成一条`ClipArchiveRequest`在数据库
    - 公众号去调用另一个我们服务, 这个服务会
        - 来下载这个链接. 从中 (尽可能) 解析出标题title, 作者author, 媒体publisher, 年月date. 把这些都入库
        - 并且把这些信息返回给lanting-server
        - 这部分我来写, 调用者lanting-server这边先mock
    - 把所有获取到的数据, 返回成一条消息给用户
2. 用户此时可以通过点公众号下面的菜单 (这个菜单可以在公众号管理后台配置) 来编辑这条archive的信息
    - 能编辑的包括tags, remarks, author, publisher, date, title所有这些字段
3. 之后如果用户又发了另一条链接url, 就不能再从公众号编辑之前的archive的信息了. 就变成编辑新的

[x] searcch user by nickname
[x] perf 1st user vs perf 900m user
[x] profiling - time spent on http request / controller / DB query
[x] redis cache
- logging
- auth