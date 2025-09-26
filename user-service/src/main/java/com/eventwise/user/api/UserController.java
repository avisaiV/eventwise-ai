package com.eventwise.user.api;


import com.eventwise.user.domain.User;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


import java.util.*;


@RestController
@RequestMapping("/users")
public class UserController {
private final Map<Long, User> db = new HashMap<>();
private long seq = 1L;


@PostMapping
public ResponseEntity<User> create(@RequestBody User u) {
u.setId(seq++); db.put(u.getId(), u); return ResponseEntity.ok(u);
}


@GetMapping("/{id}")
public ResponseEntity<User> get(@PathVariable long id) {
var u = db.get(id); return u==null? ResponseEntity.notFound().build(): ResponseEntity.ok(u);
}
}