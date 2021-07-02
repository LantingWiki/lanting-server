## ClipArchive 添加文章工作流

涉及的微信接口: https://developers.weixin.qq.com/doc/offiaccount/Custom_Menus/Creating_Custom-Defined_Menu.html
自定义菜单, 消息管理, 消息回复A

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

# Proper backend

其他的metadata都在comments里. 这个方式本身是有很多优点的
- 编辑方便
- 检索方便

现在问题是什么呢. 问题在于 1. 存储在github上, 同步机制用的git. github不可靠. git一系列命令也不怎么好

拆解成2条
1. 是github时不时连不上, 且不能用来存blob origs. 这个可以换成OSS, 
2. git同步机制写起来麻烦. 这个就要设计一下了. 改了什么就上传, cp -u, 应该能解决
3. 所以其他的好多东西本质还是不用入库. 只是需要好的备份机制. 考虑一下
4. 游戏对应的origs, 之前一直是本地读文件来判断. 这样就不行了, 要么库里要么OSS里. 要考虑一下

根据现在的工作流, 如果是电脑端保存, 创建一个comments文件, 然后编辑, 然后有一个命令和OSS同步. 这样应该可以

origs怎么办? 
