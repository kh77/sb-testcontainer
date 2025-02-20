package com.sm.service;

import com.sm.exceptions.UsersServiceException;
import com.sm.dao.User;
import com.sm.dao.UsersRepository;
import com.sm.shared.UserDto;
import org.modelmapper.ModelMapper;
import org.modelmapper.TypeToken;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service("usersService")
public class UsersServiceImpl implements UsersService {

    private final UsersRepository usersRepository;
    private final BCryptPasswordEncoder bCryptPasswordEncoder;

    public UsersServiceImpl(UsersRepository usersRepository, BCryptPasswordEncoder bCryptPasswordEncoder) {
        this.usersRepository = usersRepository;
        this.bCryptPasswordEncoder = bCryptPasswordEncoder;
    }

    @Override
    public UserDto createUser(UserDto user) {

        if (usersRepository.findByEmailEndsWith(user.getEmail()) != null)
            throw new UsersServiceException("Record already exists");

        ModelMapper modelMapper = new ModelMapper();
        User userEntity = modelMapper.map(user, User.class);

        String publicUserId = UUID.randomUUID().toString();
        userEntity.setUserId(publicUserId);
        userEntity.setEncryptedPassword(bCryptPasswordEncoder.encode(user.getPassword()));

        User storedUserDetails = usersRepository.save(userEntity);

        UserDto returnValue  = modelMapper.map(storedUserDetails, UserDto.class);

        return returnValue;
    }

    @Override
    public List<UserDto> getUsers(int page, int limit) {
        List<UserDto> list;

        if (page > 0) page -=1;

        Pageable pageableRequest = PageRequest.of(page, limit);
        Page<User> usersPage = usersRepository.findAll(pageableRequest);
        List<User> users = usersPage.getContent();

        Type listType = new TypeToken<List<UserDto>>() {}.getType();
        list = new ModelMapper().map(users, listType);

        return list;
    }

    @Override
    public UserDto getUser(String email) {
        User user = usersRepository.findByEmailEndsWith(email);

        if (user == null)
            throw new UsernameNotFoundException(email);

        UserDto returnValue = new UserDto();
        BeanUtils.copyProperties(user, returnValue);

        return returnValue;
    }

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        User user = usersRepository.findByEmailEndsWith(email);

        if (user == null)
            throw new UsernameNotFoundException(email);

        return new org.springframework.security.core.userdetails.User(user.getEmail(), user.getEncryptedPassword(), new ArrayList<>());
    }

}
