/*
 * The Exomiser - A tool to annotate and prioritize variants
 *
 * Copyright (C) 2012 - 2016  Charite Universitätsmedizin Berlin and Genome Research Ltd.
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU Affero General Public License as
 *  published by the Free Software Foundation, either version 3 of the
 *  License, or (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU Affero General Public License for more details.
 *
 *  You should have received a copy of the GNU Affero General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.charite.compbio.exomiser.core.dao;

import de.charite.compbio.exomiser.core.model.PhenotypeMatch;
import de.charite.compbio.exomiser.core.model.PhenotypeTerm;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collections;
import java.util.Set;

/**
 *
 * @author Jules Jacobsen <jules.jacobsen@sanger.ac.uk>
 */
@Repository
public class HumanPhenotypeOntologyDao implements OntologyDao {

    private static final Logger logger = LoggerFactory.getLogger(HumanPhenotypeOntologyDao.class);

    @Autowired
    private DataSource dataSource;

    @Override
    public Set<PhenotypeTerm> getAllTerms() {
        String query = "select id, lcname as term from hpo";
        try (
                Connection connection = dataSource.getConnection();
                PreparedStatement ontologyTermsStatement = connection.prepareStatement(query);
                ResultSet rs = ontologyTermsStatement.executeQuery()) {

            return OntologyDaoResultSetProcessor.processOntologyTermsResultSet(rs);
            
        } catch (SQLException e) {
            logger.error("Unable to execute query '{}' for HPO terms", query, e);
        }
        return Collections.emptySet();
    }

    @Override
    public Set<PhenotypeMatch> getPhenotypeMatchesForHpoTerm(PhenotypeTerm hpoTerm) {
        String mappingQuery = "SELECT simj, ic, score, hp_id_hit AS hit_id, hp_hit_term AS hit_term, lcs_id, lcs_term FROM hp_hp_mappings WHERE hp_id = ?";
        try (
                Connection connection = dataSource.getConnection();
                PreparedStatement ps = setQueryHpId(connection, mappingQuery, hpoTerm);
                ResultSet rs = ps.executeQuery()) {

            return OntologyDaoResultSetProcessor.processOntologyTermMatchResultSet(rs, hpoTerm);
            
        } catch (SQLException e) {
            logger.error("Unable to execute query '{}' for HP-HP match terms", mappingQuery, e);
        }
        return Collections.emptySet();
    }

    private PreparedStatement setQueryHpId(final Connection connection, String mappingQuery, PhenotypeTerm hpoTerm) throws SQLException {
        PreparedStatement ps = connection.prepareStatement(mappingQuery);
        ps.setString(1, hpoTerm.getId());
        return ps;
    }
    
}
