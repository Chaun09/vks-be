package vn.eledevo.vksbe.utils;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.ContextClosedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Component
public class ApplicationShutdownListener {
    @Autowired
    private JdbcTemplate jdbcTemplate;

    @EventListener
    public void onApplicationEvent(ContextClosedEvent event) {
        //        jdbcTemplate.execute("SET FOREIGN_KEY_CHECKS = 0;");
        //        // Xóa dữ liệu trong bảng khi ứng dụng tắt
        //        jdbcTemplate.execute("TRUNCATE TABLE account_case");
        //        jdbcTemplate.execute("TRUNCATE TABLE accounts");
        //        jdbcTemplate.execute("TRUNCATE TABLE  authtokens");
        //        jdbcTemplate.execute("TRUNCATE TABLE case_person");
        //        jdbcTemplate.execute("TRUNCATE TABLE  case_status");
        //        jdbcTemplate.execute("TRUNCATE TABLE caseflow");
        //        jdbcTemplate.execute("TRUNCATE TABLE  cases");
        //        jdbcTemplate.execute("TRUNCATE TABLE citizens");
        //        jdbcTemplate.execute("TRUNCATE TABLE  computers");
        //        jdbcTemplate.execute("TRUNCATE TABLE departments");
        //        jdbcTemplate.execute("TRUNCATE TABLE  documents");
        //        jdbcTemplate.execute("TRUNCATE TABLE histories");
        //        jdbcTemplate.execute("TRUNCATE TABLE mindmap_template");
        //        jdbcTemplate.execute("TRUNCATE TABLE organizations");
        //        jdbcTemplate.execute("TRUNCATE TABLE profiles");
        //        jdbcTemplate.execute("TRUNCATE TABLE roles");
        //        jdbcTemplate.execute("TRUNCATE TABLE usbs");
        //        System.out.println("Truncate data table.");
    }
}
