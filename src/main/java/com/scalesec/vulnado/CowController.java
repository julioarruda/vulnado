package com.scalesec.vulnado;

import org.springframework.web.bind.annotation.*;
import org.springframework.boot.autoconfigure.*;

import java.io.Serializable;

@RestController
@EnableAutoConfiguration
public class CowController {
    @RequestMapping(value = "/cowsay")
    String cowsay(@RequestParam(defaultValue = "I love Linux!") String input) {
        if (!isValidInput(input)) {
            throw new BadRequest("Invalid input");
        }
        return Cowsay.run(input);
    }

    private boolean isValidInput(String input) {
        String regex = "^[a-zA-Z0-9_@!#%& ]+$";
        return input.matches(regex);
    }
}

@ResponseStatus(HttpStatus.BAD_REQUEST)
class BadRequest extends RuntimeException {
    public BadRequest(String exception) {
        super(exception);
    }
}
