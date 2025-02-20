package com.sm.controller;

import com.sm.dao.User;
import com.sm.service.UsersService;
import com.sm.shared.UserDto;
import com.sm.controller.request.UserDetailsRequestModel;
import com.sm.controller.response.UserResponse;
import jakarta.validation.Valid;
import org.modelmapper.ModelMapper;
import org.modelmapper.TypeToken;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/users")
public class UsersController {

    private final UsersService usersService;

    public UsersController(UsersService usersService) {
        this.usersService = usersService;
    }

    @PostMapping
    public UserResponse createUser(@RequestBody @Valid UserDetailsRequestModel userDetails) throws Exception {
        UserDto userDto = new UserDto();
        BeanUtils.copyProperties(userDetails, userDto);
        UserDto createdUser = usersService.createUser(userDto);
        UserResponse response = new UserResponse();
        BeanUtils.copyProperties(createdUser, response);
        return response;
    }

    @GetMapping
    public List<UserResponse> getUsers(@RequestParam(value = "page", defaultValue = "0") int page,
                                       @RequestParam(value = "limit", defaultValue = "10") int limit) {
        List<UserDto> users = usersService.getUsers(page, limit);
        Type response = new TypeToken<List<UserResponse>>() {}.getType();
        List listData = new ModelMapper().map(users, response);
        return listData;
    }
}
