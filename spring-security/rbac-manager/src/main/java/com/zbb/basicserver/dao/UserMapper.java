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
            "values (#{username},#{encryptedPassword},now(),now())")
    void createUser(@Param("username") String username, @Param("encryptedPassword") String encryptedPassword);

    @Select("select *\n" +
            "from USER where USERNAME = #{username}")
    User findUserByUsername(@Param("username") String username);


    @Select("select SR.*\n" +
            "from USER U\n" +
            "         left join SYS_ROLE_USER SRU on U.ID = SRU.USER_ID\n" +
            "         left join SYS_ROLE SR on SR.ID = SRU.ROLE_ID\n" +
            "WHERE USERNAME = #{username}")
    List<Role> getRolesByUsername(@Param("username") String username);

    @Select({"<script>\n" +
            "    select SP.*\n" +
            "    from SYS_ROLE\n" +
            "    left join SYS_PERMISSION_ROLE SPR on SYS_ROLE.ID = SPR.ROLE_ID\n" +
            "    left join SYS_PERMISSION SP on SP.ID = SPR.PERMISSION_ID\n" +
            "    where ROLE_ID in\n" +
            "    <foreach item=\"role\" collection=\"roles\"\n" +
            "             open=\"(\" separator=\",\" close=\")\">\n" +
            "        #{role}\n" +
            "    </foreach>\n" +
            "</script>\n"})
    List<Permission> getPermissionsByRoles(@Param("roles") List<Integer> roles);

    @Select("select * from SYS_PERMISSION")
    List<Permission> getAllPermissions();
}
