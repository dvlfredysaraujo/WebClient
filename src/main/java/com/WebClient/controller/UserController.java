package com.WebClient.controller;

import com.WebClient.dto.UserDto;
import com.WebClient.service.UserService;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("proxy/users")
public class UserController {
    private final UserService userService;

    public UserController(UserService userService){
        this.userService = userService;
    }

    @GetMapping
    public Flux<UserDto> list(){
        return userService.list();
    }

    @GetMapping("/{id}")
    public Mono<UserDto> getById(@PathVariable Long id){
        return userService.getById(id);
    }

    @PostMapping
    public Mono<UserDto> create(@RequestBody UserDto newUserDto){
        return userService.create(newUserDto);
    }

    @PutMapping("/{id}")
    public Mono<UserDto> update(@PathVariable Long id, @RequestBody UserDto userDto){
        return userService.update(id, userDto);
    }

    @DeleteMapping("/{id}")
    public Mono<Void> delete(@PathVariable Long id){
        return userService.delete(id);
    }

}
