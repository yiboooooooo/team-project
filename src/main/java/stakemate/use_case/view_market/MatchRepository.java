package stakemate.use_case.view_market;

import java.util.List;

import stakemate.entity.Match;

public interface MatchRepository {
    /**
     * Retrieves all available matches from the data source.
     *
     * @return a list of all matches.
     * @throws RepositoryException if an error occurs while fetching data.
     */
    List<Match> findAllMatches() throws RepositoryException;
}
