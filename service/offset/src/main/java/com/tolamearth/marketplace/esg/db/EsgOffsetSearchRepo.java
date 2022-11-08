/*
 * Copyright 2022 Tolam Earth
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.tolamearth.marketplace.esg.db;

import com.tolamearth.marketplace.esg.EsgOffsetSummary;
import com.tolamearth.marketplace.offset.Nft;
import io.micronaut.data.jdbc.annotation.JdbcRepository;
import io.micronaut.data.jdbc.runtime.JdbcOperations;
import io.micronaut.data.model.query.builder.sql.Dialect;
import jakarta.inject.Singleton;

import javax.transaction.Transactional;
import java.math.BigInteger;
import java.sql.ResultSet;
import java.util.*;
import java.util.stream.Collectors;

@Singleton
@JdbcRepository(dialect = Dialect.H2)
public class EsgOffsetSearchRepo {

    private final JdbcOperations jdbc;

    public EsgOffsetSearchRepo(JdbcOperations jdbc){
        this.jdbc = jdbc;
    }
    /*
    Loop through the map of TITLEs and corresponding VALUEs finding the matching Nfts,
    placing them in a set, and then intersecting the existing set with the next set.
     */
    public List<EsgOffsetSummary> findByParameters(Map<String, List<String>> parameters){
        if(parameters.size() == 0){
            return runFinalQuery(null, buildFinalQuery(0)).stream().map(teos -> transmogrify(teos)).collect(Collectors.toList());//make final query
        }
        List<EsgOffsetSummary> summaries = new ArrayList<>();
        String firstKey = parameters.keySet().stream().findFirst().get();
        var query = buildPropertyQuery(parameters.get(firstKey).size());
        Set<Nft> nfts = runPropertyQuery(query, firstKey, parameters.get(firstKey));
        if(parameters.size() > 1) {
            parameters.keySet().stream().skip(1).forEach( key ->
                    {
                        Set<Nft> newSet = runPropertyQuery(buildPropertyQuery(parameters.get(key).size()), key, parameters.get(key));
                        nfts.retainAll(newSet);//Take the intersection of the two sets (i.e. perform the "AND")
                    }
            );
        }
        if(nfts.size() > 0){
            summaries = runFinalQuery(nfts, buildFinalQuery(nfts.size())).stream().map(teos -> transmogrify(teos)).collect(Collectors.toList());//make final query
        }
        return summaries;
    }
    private static String buildPropertyQuery(int length){
        StringBuilder sbuf = new StringBuilder("""
        SELECT \
        b.token_id as token_id, \
        b.serial_number as serial_number \
        FROM ESGOFFSETATTRIBUTE a \
        JOIN ESGOFFSET b ON a.ESG_OFFSET_ID=b.ID \
        WHERE title=?  AND value IN ( \
        """);
        if(length > 1){
            sbuf.append("?");
            for(int i=0;i<length-1;i++){
                sbuf.append(",?");
            }
        }else{
            sbuf.append("?");
        }
        sbuf.append(");");
        return sbuf.toString();
    }
    @Transactional
    public Set<Nft> runPropertyQuery(String query, String key, List<String> parameters){
        return jdbc.prepareStatement(query, statement -> {
            statement.setString(1, key);
            for(int i=0;i<parameters.size();i++){
                statement.setString(i+2, parameters.get(i));
            }
            ResultSet rs = statement.executeQuery();
            var set = new HashSet<Nft>();
            while (rs.next()){
                set.add(new Nft(rs.getString("token_id"), rs.getLong("serial_number")));
            }
            return set;
        });
    }
    @Transactional
    public List<TempEsgOffsetSummary> runFinalQuery(Set<Nft> nfts, String query){
        return jdbc.prepareStatement(query, statement -> {
            if (nfts != null && nfts.size() > 0){
                int index=1;
                for (Nft nft : nfts) {
                    statement.setString(index++, nft.tokenId());
                    statement.setLong(index++, nft.serialNumber());
                }
            }
            ResultSet rs = statement.executeQuery();
            TempEsgOffsetSummary tempEsgOffsetSummary;
            Nft nft;
            Map<Nft,TempEsgOffsetSummary> tempMap = new HashMap<>();
            while (rs.next()){
                nft = new Nft(rs.getString("token_id"), rs.getLong("serial_number"));
                tempEsgOffsetSummary = tempMap.containsKey(nft) ?
                        tempMap.get(nft) :
                        new TempEsgOffsetSummary(nft, rs.getString("owner_id"), rs.getBigDecimal("price").toBigInteger());
                mapTo(tempEsgOffsetSummary, rs.getString("title"), rs.getString("value"));
                tempMap.put(nft, tempEsgOffsetSummary);
            }
            return tempMap.values().stream().toList();
        });
    }
    private void mapTo(TempEsgOffsetSummary tempEsgOffsetSummary, String title, String value){
        switch (title){
            case "Project Category":
                tempEsgOffsetSummary.category = value;
                break;
            case "Project Type":
                tempEsgOffsetSummary.type = value;
                break;
            case "Project Name":
                tempEsgOffsetSummary.name = value;
                break;
            case "Project Country":
                tempEsgOffsetSummary.country = value;
                break;
            case "Project Region":
                tempEsgOffsetSummary.region = value;
                break;
            case "Vintage":
                tempEsgOffsetSummary.vintage = value;
                break;
        }
    }
    /*
    Get the attributes of all matching Nfts (tokenId/serial#)
     */
    private String buildFinalQuery(int length){
        StringBuilder sbuf = new StringBuilder("""
        SELECT \
        a.account_id as owner_id, \
        a.token_id as token_id, \
        a.serial_number as serial_number, \
        a.retail_price as price, \
        c.title as title, \
        c.value as value \
        FROM ListedOffsets a \
        JOIN ESGOFFSET b ON a.token_id=b.token_id AND a.serial_number=b.serial_number \
        JOIN ESGOFFSETATTRIBUTE c ON b.ID=c.ESG_OFFSET_ID \
        WHERE a.purchase_txn_id IS NULL \
        """);
        if(length > 0) {
            sbuf.append(" AND ((a.token_id=? AND a.serial_number=?) ");
            if(length > 1){
                for(int i=0;i<length-1;i++) {
                    sbuf.append(" OR (a.token_id=? AND a.serial_number=?)");
                }
            }
            sbuf.append(")");
        }
        sbuf.append(";");
        return sbuf.toString();
    }
    class TempEsgOffsetSummary{
        TempEsgOffsetSummary(Nft nft, String owner_id, BigInteger price){
            this.nft = nft;
            this.owner_id = owner_id;
            this.price = price;
        }
        String owner_id;
        Nft nft;
        BigInteger price;
        String category;
        String type;
        String name;
        String country;
        String region;
        String vintage;
    }
    private EsgOffsetSummary transmogrify(TempEsgOffsetSummary teos){
        return new EsgOffsetSummary(teos.owner_id, teos.nft, teos.price.longValue(), teos.category, teos.type, teos.name, teos.country, teos.region, teos.vintage);
    }
}
