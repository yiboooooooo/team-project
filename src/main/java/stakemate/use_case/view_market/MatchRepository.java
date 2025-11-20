package stakemate.use_case.view_market;

import java.util.List;

import stakemate.entity.Match;

public interface MatchRepository {
    List<Match> findAllMatches() throws RepositoryException;
}
