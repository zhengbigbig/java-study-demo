package com.zbb.basicserver.dao;

import com.zbb.basicserver.entity.Permission;
import com.zbb.basicserver.entity.Role;
import com.zbb.basicserver.entity.User;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface UserMapper {

    @Insert("insert into USER (USERNAME, ENCRYPTED_PASSWORD, CREATED_AT, UPDATED_AT)\n" +
            "values (#{username},#{encryptedPassword},#{createdAt},#{updatedAt});")
    int createUser(@Param("user") User user);

    @Select("select *\n" +
            "from USER where USERNAME = #{username}")
    User findUserByUsername(@Param("username") String username);

    @Select("select SR.*\n" +
            "from USER U\n" +
            "         left join SYS_ROLE_USER SRU on U.ID = SRU.USER_ID\n" +
            "         left join SYS_ROLE SR on SR.ID = SRU.ROLE_ID\n" +
            "WHERE USERNAME = #{username}")
    List<Role> getRolesByUsername(@Param("username") String username);

    @Select("select SP.*\n" +
            "from USER U\n" +
            "         left join SYS_ROLE_USER SRU on U.ID = SRU.USER_ID\n" +
            "         left join SYS_ROLE SR on SR.ID = SRU.ROLE_ID\n" +
            "         left join SYS_PERMISSION_ROLE SPR on SRU.ROLE_ID = SPR.ROLE_ID\n" +
            "         left join SYS_PERMISSION SP on SP.ID = SPR.PERMISSION_ID\n" +
            "WHERE USERNAME = #{username}\n" +
            "group by sp.ID")
    List<Permission> getPermissionsByUsername(@Param("username") String username);

    @Select("select * from SYS_PERMISSION")
    List<Permission> getAllPermissions();
}
