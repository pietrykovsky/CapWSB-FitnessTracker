package pl.wsb.fitnesstracker.report.internal;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.stereotype.Service;
import pl.wsb.fitnesstracker.training.api.Training;
import pl.wsb.fitnesstracker.training.api.TrainingProvider;
import pl.wsb.fitnesstracker.user.api.User;
import pl.wsb.fitnesstracker.user.api.UserProvider;

import java.util.concurrent.TimeUnit;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;

@EnableScheduling
@Service
class TrainingReportService {

    private static final Logger log = LoggerFactory.getLogger(TrainingReportService.class);

    private final UserProvider userProvider;
    private final TrainingProvider trainingProvider;

    public TrainingReportService(UserProvider userProvider, TrainingProvider trainingProvider) {
        this.userProvider = userProvider;
        this.trainingProvider = trainingProvider;
    }

    // Run every week, starting from application startup
    @Scheduled(fixedRate = 7, timeUnit=TimeUnit.DAYS)
    public void generateWeeklyReport() {
        log.info("Starting weekly training report generation...");
        List<User> users = userProvider.findAllUsers();
        Date oneWeekAgo = Date.from(LocalDate.now().minusWeeks(1).atStartOfDay(ZoneId.systemDefault()).toInstant());

        for (User user : users) {
            List<Training> userTrainings = trainingProvider.getTrainingsByUserId(user.getId());
            
            // Filter for last week
            List<Training> weeklyTrainings = userTrainings.stream()
                    .filter(t -> t.getEndTime().after(oneWeekAgo))
                    .toList();

            log.info("Weekly report for user {}: {} trainings", user.getEmail(), weeklyTrainings.size());
            weeklyTrainings.forEach(t -> log.info(" - Training on {}: {} km", t.getEndTime(), t.getDistance()));
        }
        log.info("Weekly training report generation completed.");
    }
}
