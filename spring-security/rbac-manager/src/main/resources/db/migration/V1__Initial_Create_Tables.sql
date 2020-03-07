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

insert into USER (USERNAME, ENCRYPTED_PASSWORD, CREATED_AT, UPDATED_AT)
values ('admin','password',now(),now());

insert into sys_role (role, name)
values ('管理员', 'ROLE_ADMIN');
insert into sys_role (role, name)
values ('普通用户', 'ROLE_USER');

insert into SYS_ROLE_USER (USER_ID, ROLE_ID)
values (1, 1);
insert into SYS_ROLE_USER (USER_ID, ROLE_ID)
values (1, 2);
insert into SYS_PERMISSION_ROLE (ROLE_ID, PERMISSION_ID)
values (1, 1);
insert into SYS_PERMISSION_ROLE (ROLE_ID, PERMISSION_ID)
values (1, 2);
insert into SYS_PERMISSION_ROLE (ROLE_ID, PERMISSION_ID)
values (2, 2);
insert into sys_permission (name, description, url, pid)
values ('MANAGER', '管理', '/manager/**', null);
insert into sys_permission (name, description, url, pid)
values ('user', '用户个人信息', '/user/**', null);