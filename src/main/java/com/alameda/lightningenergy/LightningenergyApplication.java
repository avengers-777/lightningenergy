package com.alameda.lightningenergy;

import com.alameda.lightningenergy.utils.SpringContextUtil;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Import;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@Import(SpringContextUtil.class)
@EnableTransactionManagement
@EnableScheduling
@SpringBootApplication
public class LightningenergyApplication {

	public static void main(String[] args) {
		SpringApplication.run(LightningenergyApplication.class, args);
	}

}
