package stakemate.use_case.view_market;

import stakemate.entity.Match;

import java.util.List;

public interface MatchRepository {
    List<Match> findAllMatches() throws RepositoryException;
}
