package com.fiap.tech04;

import java.time.Instant;
import java.util.List;

interface FeedbackRepository {
    void save(Feedback feedback);

    List<Feedback> between(Instant start, Instant end);
}
