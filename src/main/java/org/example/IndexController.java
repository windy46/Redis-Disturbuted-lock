package org.example;

import org.redisson.Redisson;
import org.redisson.api.RLock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

@RestController
public class IndexController {

    @Autowired
    private Redisson redisson;

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    @RequestMapping("/deduct_stock")
    public String deductStock() {

        String lockKey = "lockKey";

        String clientId = UUID.randomUUID().toString();

        try {
            Boolean result = stringRedisTemplate.opsForValue().setIfAbsent(lockKey, "windy", 30, TimeUnit.SECONDS);
            if (!result) {
                return "error_code";
            }
            int stock = Integer.parseInt(stringRedisTemplate.opsForValue().get("stock"));
            if (stock > 0) {
                int realStock = stock - 1;
                stringRedisTemplate.opsForValue().set("stock", realStock + "");
                System.out.println("扣减成功，剩余库存：" + realStock);
            } else {
                System.out.println("扣减失败，库存不足");
            }
        } finally {
            if(clientId.equals(stringRedisTemplate.opsForValue().get(lockKey))) {
                stringRedisTemplate.delete(lockKey);
            }
        }
        return "end";
    }


}
