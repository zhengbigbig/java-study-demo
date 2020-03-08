create table user
(
    id                 bigint primary key auto_increment,
    username           varchar(100) unique not null,
    encrypted_password varchar(100)        not null,
    enabled            tinyint             not null default 1 comment '账户是否可用',
    created_at         timestamp,
    updated_at         timestamp
);

create table sys_role
(
    id   bigint primary key auto_increment,
    role varchar(100) not null,
    name varchar(100) not null
);
create table sys_role_user
(
    id      bigint primary key auto_increment,
    user_id bigint not null,
    role_id bigint not null
);

create table sys_permission_role
(
    id            bigint primary key auto_increment,
    role_id       bigint not null,
    permission_id bigint not null
);

create table sys_permission
(
    id          bigint primary key auto_increment,
    name        varchar(100) not null,
    description varchar(100) not null,
    url         varchar(100) not null,
    pid         int
);

insert into sys_role (role, name)
values ( 'ADMIN','管理员');
insert into sys_role (role, name)
values ('USER','普通用户');

insert into SYS_ROLE_USER (USER_ID, ROLE_ID)
values (1, 1);
insert into SYS_ROLE_USER (USER_ID, ROLE_ID)
values (2, 2);

insert into SYS_PERMISSION_ROLE (ROLE_ID, PERMISSION_ID)
values (1, 1);
insert into SYS_PERMISSION_ROLE (ROLE_ID, PERMISSION_ID)
values (1, 2);
insert into SYS_PERMISSION_ROLE (ROLE_ID, PERMISSION_ID)
values (1, 3);
insert into SYS_PERMISSION_ROLE (ROLE_ID, PERMISSION_ID)
values (2, 3);

insert into sys_permission (name, description, url, pid)
values ('sys:log', '管理', '/syslog', null);
insert into sys_permission (name, description, url, pid)
values ('sys:user', '管理', '/sysuser', null);
insert into sys_permission (name, description, url, pid)
values ('index', '主页', '/index', null);

CREATE TABLE `persistent_logins`
(
    `username`  varchar(64)  NOT NULL,
    `series`    varchar(64) NOT NULL,
    `token`     varchar(64) NOT NULL,
    `last_used` timestamp   NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`series`)
);
