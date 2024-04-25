package com.alameda.lightningenergy.utils;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.TextCriteria;
import org.springframework.data.mongodb.core.schema.JsonSchemaObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Pattern;

import static com.alameda.lightningenergy.entity.common.Common.ID_PROPERTIES;


@Data
public class QueryTool {

    private  List<QueryParameters> queryList;

    public QueryTool(){
        this.queryList = new ArrayList<>();
    }
    public QueryTool(List<QueryParameters> queryList){
        assert queryList != null;
        this.queryList = queryList;
    }
    public QueryTool addCondition(QueryParameters queryParameters){
        this.queryList.add(queryParameters);
        return this;
    }
    public QueryTool findById(String id){
        addCondition(new QueryTool.QueryParameters(QueryTool.QueryType.IN,ID_PROPERTIES, List.of(id)));
        return this;
    }
    public Query build(){
        Query query = new Query();
        for (QueryParameters queryParameters : this.queryList) {
            switch (queryParameters.getType()) {
                case IN -> {

                    query.addCriteria(Criteria.where(queryParameters.getWhere()).in(queryParameters.getArgs()));
                }
                case NIN -> {

                    query.addCriteria(Criteria.where(queryParameters.getWhere()).nin(queryParameters.getArgs()));
                }

                case IN_LIST -> {

                    query.addCriteria(Criteria.where(queryParameters.getWhere()).elemMatch(Criteria.where(queryParameters.getArgs().get(0).toString()).in(queryParameters.getArgs())));
                }
                case TYPE -> {
                    // 对应于 MongoDB 的 $type 操作符。
                    // 这个查询条件会匹配字段类型为指定 BSON 类型的所有文档。
                    // 参数是 JsonSchemaObject.Type 枚举的一个值，表示指定的 BSON 类型。
                    query.addCriteria(Criteria.where(queryParameters.getWhere()).type((JsonSchemaObject.Type) queryParameters.getArgs().get(0)));
                }

                case EXISTS -> {
                    // 对应于 MongoDB 的 $exists 操作符。
                    // 这个查询条件会根据字段的存在性匹配文档。
                    // 如果参数是 true，那么返回包含指定字段的所有文档。
                    // 如果参数是 false，那么返回不包含指定字段的所有文档。
                    query.addCriteria(Criteria.where(queryParameters.getWhere()).exists((Boolean) queryParameters.getArgs().get(0)));
                }

                case LIST_SIZE -> {
                    query.addCriteria(Criteria.where(queryParameters.getWhere()).size((Integer) queryParameters.getArgs().get(0)));
                }
                case IN_FUZZY -> {
                    Pattern patternFuzzy = Pattern.compile("^.*" + queryParameters.getArgs().get(0) + ".*$", Pattern.CASE_INSENSITIVE);
                    query.addCriteria(Criteria.where(queryParameters.getWhere()).regex(patternFuzzy));
                }
                case REGEX -> {


                    Pattern pattern = Pattern.compile((String) queryParameters.getArgs().get(0));
                    query.addCriteria(Criteria.where(queryParameters.getWhere()).regex(pattern));
                }
                case TEXT -> {
                    TextCriteria criteria = TextCriteria.forDefaultLanguage().matching((String) queryParameters.getArgs().get(0));
                    query.addCriteria(criteria);
                }

                case GTE -> {
                    query.addCriteria(Criteria.where(queryParameters.getWhere()).gte(queryParameters.getArgs().get(0)));
                }
                case GT -> {
                    query.addCriteria(Criteria.where(queryParameters.getWhere()).gt(queryParameters.getArgs().get(0)));
                }

                case LTE -> {

                    query.addCriteria(Criteria.where(queryParameters.getWhere()).lte(queryParameters.getArgs().get(0)));
                }
                case LT -> {

                    query.addCriteria(Criteria.where(queryParameters.getWhere()).lt(queryParameters.getArgs().get(0)));
                }

                case NE -> {
                    query.addCriteria(Criteria.where(queryParameters.getWhere()).ne(queryParameters.getArgs().get(0)));
                }
                case GTE_AND_LTE -> {
                    query.addCriteria(Criteria.where(queryParameters.getWhere()).gte(queryParameters.getArgs().get(0)).lte(queryParameters.getArgs().get(1)));
                }
                case GT_AND_LT -> {
                    query.addCriteria(Criteria.where(queryParameters.getWhere()).gt(queryParameters.getArgs().get(0)).lt(queryParameters.getArgs().get(1)));
                }
                case NOT_NUll -> {
                    query.addCriteria(Criteria.where(queryParameters.getWhere()).ne(null));
                }
                case SET_NO_EMPTY -> {
                    query.addCriteria(Criteria.where(queryParameters.getWhere()).exists(true).ne(Collections.emptySet()));
                }


            }
        }

        return query;

    }
    public static Pageable setSort(int page,int size, Sort.Direction direction,String properties){
        return PageRequest.of(page, size, Sort.by(direction,properties));
    }
    public enum QueryType {
        IN_FUZZY,IN,NIN,REGEX,GTE,LTE,NE,GTE_AND_LTE,NOT_NUll,TEXT,IN_LIST,EXISTS,TYPE,LIST_SIZE,SET_NO_EMPTY,GT,LT,GT_AND_LT
    }
    @AllArgsConstructor
    @Data
    @NoArgsConstructor
    public static class QueryParameters {
        private  QueryType type;
        private  String where;
        private  List args;

        public QueryParameters(QueryType type, String where) {
            this.type = type;
            this.where = where;
            this.args = new ArrayList<>();
        }
        public QueryParameters addArg(Object arg){
            this.args.add(arg);
            return this;
        };
    }



}
