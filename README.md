## 1.项目描述
- 项目旨在为在校师生提供一个学术分享讨论的平台。主要功能： 用户可以通过注册、登录获取不同权限，用户可
以进行发帖、评论、私信、上传文件、 点赞、关注、搜索、查看热帖排行等操作，管理员能够对后台数据进行统计和维护。
## 2.项目各功能介绍和技术实现笔记
### [1. 项目环境搭建](https://github.com/Harrison-2021/community2/tree/master/notes/1.%E9%A1%B9%E7%9B%AE%E5%87%86%E5%A4%87%EF%BC%88%E7%8E%AF%E5%A2%83%E6%90%AD%E5%BB%BA%EF%BC%89)
### [2. 开发社区注册登录模块](https://github.com/Harrison-2021/community2/tree/master/notes/2.%E5%BC%80%E5%8F%91%E7%A4%BE%E5%8C%BA%E7%99%BB%E5%BD%95%E6%B3%A8%E5%86%8C%E6%A8%A1%E5%9D%97)
- 使用MD5加密加盐对密码储存， 基于邮件激活注册状态，使用Spring Email发送HTML邮件；使用Kaptcher生成验证码辅助登录验证；使用Session、Cookie会话管理策略记录登录状态和颁发凭证，实现请求间的交互；
使用拦截器进行登录状态检查、暂存用户信息以便于各请求获取。
### [3. 开发社区核心功能模块](https://github.com/Harrison-2021/community2/tree/master/notes/3.%E5%BC%80%E5%8F%91%E7%A4%BE%E5%8C%BA%E6%A0%B8%E5%BF%83%E5%8A%9F%E8%83%BD%E6%A8%A1%E5%9D%97)
- 使用AJAX异步发布帖子并过滤敏感词；使用Spring事务管理添加和显示评论事件。
### [4. 使用radis高性能储存方案](https://github.com/Harrison-2021/community2/tree/master/notes/4.%E4%BD%BF%E7%94%A8Redis%E9%AB%98%E6%80%A7%E8%83%BD%E5%82%A8%E5%AD%98%E6%96%B9%E6%A1%88)
- 使用redis缓存频繁访问数据以提高服务器性能；使用Redis的Set、Zset数据结构实现点赞、关注相关功能。
### [5. Kafka构建TB级异步消息系统](https://github.com/Harrison-2021/community2/tree/master/notes/5.Kafak%E6%9E%84%E5%BB%BATB%E7%BA%A7%E5%BC%82%E6%AD%A5%E6%B6%88%E6%81%AF%E7%B3%BB%E7%BB%9F)
- 使用kafka作为消息队列，封装事件对象并发布到消息队列，系统异步通知给用户。
### [6. ElasticSearch分布式引擎](https://github.com/Harrison-2021/community2/tree/master/notes/6.ElasticSearch%E5%88%86%E5%B8%83%E5%BC%8F%E5%BC%95%E6%93%8E)
- Kafka向ElasticSearch服务器异步添加数据从而可以对ES全文搜索；使用Quartz定期更新帖子分数以展现动态热帖排行。
### [7. 构建安全高效的企业服务](https://github.com/Harrison-2021/community2/tree/master/notes/7.%E6%9E%84%E5%BB%BA%E5%AE%89%E5%85%A8%E9%AB%98%E6%95%88%E7%9A%84%E4%BC%81%E4%B8%9A%E6%9C%8D%E5%8A%A1)
- SpringSecurity管理认证权限与防止CSRF攻击；使用Redis的HyperLogLog、BitMap数据结构分别统计后台独立访客、日活跃用户数据。
### [8.项目总结与部署](https://github.com/Harrison-2021/community2/tree/master/notes/8.%E9%A1%B9%E7%9B%AE%E6%80%BB%E7%BB%93%E4%B8%8E%E9%83%A8%E7%BD%B2)
- SpringBoot单元测试保证测试独立性；Spring端点监控配置与权限设置；项目部署到Linux服务器技术。
## 3. 各功能模块demo
### 1. 网页首页展示
![在这里插入图片描述](https://img-blog.csdnimg.cn/dd655e1fc22f474a8c2714055df333ea.png)
![在这里插入图片描述](https://img-blog.csdnimg.cn/bd806484f4c54f1fbd9a963576e2e79f.png)

### 2. 登录注册展示

![在这里插入图片描述](https://img-blog.csdnimg.cn/2a246c0226d34612b6a3191e250af417.png)
![在这里插入图片描述](https://img-blog.csdnimg.cn/691b9b44f9ce4ef08ca7056423e37f0c.png)
![在这里插入图片描述](https://img-blog.csdnimg.cn/61e34f400a3145cfa64edfa96f8bd063.png)
![在这里插入图片描述](https://img-blog.csdnimg.cn/62115cc845b943a790384ab53eec2d5d.png)
![在这里插入图片描述](https://img-blog.csdnimg.cn/b391986c59d24fc6b4dfedf1b1d2ee72.png)
![在这里插入图片描述](https://img-blog.csdnimg.cn/773aeb0b160d4209a88bc880e421c769.png)


### 3. 发帖与评论展示

![在这里插入图片描述](https://img-blog.csdnimg.cn/572fb86342334a05a34045358b6acf5d.png)
![在这里插入图片描述](https://img-blog.csdnimg.cn/d6d36395417a45ccb14a227e442f57eb.png)
![在这里插入图片描述](https://img-blog.csdnimg.cn/a17ff05bd30a43e391f7d301df88b897.png)
![在这里插入图片描述](https://img-blog.csdnimg.cn/e5b74f5c3e1840e081d33c86454e1290.png)


### 4. 点赞关注展示
![在这里插入图片描述](https://img-blog.csdnimg.cn/3f1b970c8c1244968505251a89c27d13.png)
![在这里插入图片描述](https://img-blog.csdnimg.cn/c740d5dd3704484c809f7aa4cf67ab9d.png)
![在这里插入图片描述](https://img-blog.csdnimg.cn/4dd1e9a8695a4703a68bb8adf2ab7819.png)


### 5. 系统通知与私信展示
![在这里插入图片描述](https://img-blog.csdnimg.cn/7a37c64ab53746faa159b2129afc0dda.png)
![在这里插入图片描述](https://img-blog.csdnimg.cn/d69e2c9135854b1f8663fa4d758ff5ad.png)
![在这里插入图片描述](https://img-blog.csdnimg.cn/95044838343544ee884e2408bd686468.png)
![在这里插入图片描述](https://img-blog.csdnimg.cn/3dddf216e46d4c448c68c18bf088e62b.png)


### 6. 搜索功能展示
![在这里插入图片描述](https://img-blog.csdnimg.cn/3db0ad5f00e245f5be010a2223a0bb7d.png)

### 7. 后台数据统计
  - 非管理员没有权限访问
  ![在这里插入图片描述](https://img-blog.csdnimg.cn/9af911d658994029aabcee0413c322ee.png?x-oss-process=image/watermark,type_d3F5LXplbmhlaQ,shadow_50,text_Q1NETiBA5aSp55Sf5oiR5omNfn4=,size_20,color_FFFFFF,t_70,g_se,x_16)
  - 统计独立访客UV

  ![在这里插入图片描述](https://img-blog.csdnimg.cn/fbb9aae626d64a43a753850431b80167.png?x-oss-process=image/watermark,type_d3F5LXplbmhlaQ,shadow_50,text_Q1NETiBA5aSp55Sf5oiR5omNfn4=,size_20,color_FFFFFF,t_70,g_se,x_16)

  - 统计活跃用户

  ![在这里插入图片描述](https://img-blog.csdnimg.cn/f219aede0a884e3b9faf1ed7db3dbb7e.png?x-oss-process=image/watermark,type_d3F5LXplbmhlaQ,shadow_50,text_Q1NETiBA5aSp55Sf5oiR5omNfn4=,size_20,color_FFFFFF,t_70,g_se,x_16)

### 8. 权限管理与帖子排行
![在这里插入图片描述](https://img-blog.csdnimg.cn/c8e02a136deb4f7491aeb023290a9c53.png)
![在这里插入图片描述](https://img-blog.csdnimg.cn/2741a859cae04d8380aba70ceed1412e.png)
![在这里插入图片描述](https://img-blog.csdnimg.cn/ce5786256c554595b52eb8522ffcc273.png)
![在这里插入图片描述](https://img-blog.csdnimg.cn/84988fde5a3247d18f3ec0c3328e7f06.png)

