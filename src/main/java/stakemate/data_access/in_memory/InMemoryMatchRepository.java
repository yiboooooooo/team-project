package stakemate.data_access.in_memory;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import stakemate.entity.Match;
import stakemate.entity.MatchStatus;
import stakemate.use_case.view_market.MatchRepository;
import stakemate.use_case.view_market.RepositoryException;

public class InMemoryMatchRepository implements MatchRepository {

    private final List<Match> matches = new ArrayList<>();

    public InMemoryMatchRepository() {
        LocalDateTime now = LocalDateTime.now();

        matches.add(new Match("M1", "Raptors", "Lakers",
                MatchStatus.UPCOMING, now.plusHours(2)));
        matches.add(new Match("M2", "Celtics", "Bulls",
                MatchStatus.LIVE, now.minusMinutes(30)));
        matches.add(new Match("M3", "Warriors", "Nets",
                MatchStatus.CLOSED, now.minusHours(4)));
    }

    @Override
    public List<Match> findAllMatches() throws RepositoryException {
        return new ArrayList<>(matches);
    }
}
