# 短信验证码登录

## 1. 开发流程

1. 定义```SmsController```，获取短信验证码，将短信验证码保存到session，并将短信验证码下发给用户
2. 自定义短信验证码过滤器```SmsValidateFilter```对用户输入进行校验
3. 自定义短信验证码登录的过滤器```SmsAuthenticationFilter```进行权限校验

## 2. 实现(根据业务实际需求，可在注册时要求填写手机号或者后续做绑定，再用作短信登录)

- 这里直接修改为在注册时增加手机号

### 2.1 在User表增加phone字段、增加User实体的成员变量并修改sql语句
```sql
alter table USER
    add phone varchar(100) unique ;

```
将短信发送相关接口也```PermitAll``
### 2.2 