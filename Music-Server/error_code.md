# 1. 通用错误码

|状态码|	含义|
|:------|:-----:|
|200|请求成功|
|400|请求参数错误|
|401|用户校验失败|
|402|参数非法|
|403|没有权限|
|407|访问太频繁，请稍后再试|
|500|服务器内部错误|

# 2. 房间相关错误码
|状态码|	含义|
|:------|:-----:|
|804|房间不存在|
|807|房间数量限制|

# 3. 账号相关错误码
|状态码|	含义|
|:------|:-----:|
|900|账号请求失败|
|901|账号限20位[a-zA-Z0-9@._-]|
|902|昵称格式错误|
|903|密码格式错误|
|904|账号不存在|
|906|账号已注册|
|911|同一IP注册达到每日上限|