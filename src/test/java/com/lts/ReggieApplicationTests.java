package com.lts;

import com.lts.service.DishService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import javax.xml.ws.soap.Addressing;

@SpringBootTest
class ReggieApplicationTests {

    @Autowired
    private DishService dishService;

    @Test
    void contextLoads() {
    }

}
