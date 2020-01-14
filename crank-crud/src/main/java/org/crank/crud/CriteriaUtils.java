package org.crank.crud;

import java.util.HashSet;
import java.util.Set;


import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.crank.crud.criteria.Between;
import org.crank.crud.criteria.Comparison;
import org.crank.crud.criteria.Criterion;
import org.crank.crud.criteria.Group;
import org.crank.crud.criteria.Operator;
import org.crank.crud.criteria.OrderBy;
import org.crank.crud.criteria.Select;
import org.crank.crud.criteria.VerifiedBetween;
import org.crank.crud.join.EntityJoin;
import org.crank.crud.join.Join;
import org.crank.crud.join.JoinType;
import org.crank.crud.join.RelationshipFetch;
import org.crank.crud.join.RelationshipJoin;
import org.crank.crud.join.SimpleRelationshipJoin;

public class CriteriaUtils {
	
	protected static Logger logger = LogManager.getLogger(GenericDaoUtils.class);

	
	public static String constuctWhereClause(final Group group) {
		String whereClause = "";
		if (group == null || group.size() > 0) {
			whereClause = constructWhereClauseString(group, false);
		}
		return whereClause;
	}

	public static String constructWhereClauseString(Group group, boolean parens) {
		StringBuilder builder = new StringBuilder(255);
		if (group == null || group.size() == 0) {
			return "";
		} else if (group.size() == 1) {
			Criterion criterion = group.iterator().next();
			if (criterion instanceof Group) {
				Group innerGroup = (Group) criterion;
				if (innerGroup.size() == 0) {
					return "";
				}
			}
		}
		builder.append(" WHERE ");
		constructWhereClauseString(builder, group, false, null);
		return builder.toString();
	}

	public static void constructWhereClauseString(StringBuilder builder,
			Group group, boolean parens, Set<String> names) {

		if (names == null) {
			names = new HashSet<String>();
		}

		if (parens) {
			builder.append(" ( ");
		}
		if (group.size() == 1) {
			Criterion criterion = group.iterator().next();
			if (criterion instanceof Group) {
				constructWhereClauseString(builder, (Group) criterion, true,
						names);
			} else if (criterion instanceof Comparison) {
				addComparisonToQueryString((Comparison) criterion, builder,
						names);
			}
		} else {
			int size = group.size();
			int index = 0;
			for (Criterion criterion : group) {
				index++;
				if (criterion instanceof Group) {
					constructWhereClauseString(builder, (Group) criterion,
							true, names);
				} else if (criterion instanceof Comparison) {
					addComparisonToQueryString((Comparison) criterion, builder,
							names);
				}
				if (index != size) {
					builder.append(" ");
					builder.append(group.getJunction());
					builder.append(" ");
				}
			}
		}
		if (parens) {
			builder.append(" ) ");
		}
	}
	
	public static void addComparisonToQueryString(Comparison comparison,
			StringBuilder builder, Set<String> names) {

		String var = ":" + ditchDot(comparison.getName());
		var = ensureUnique(names, var);

		if (comparison.isObjectIdentity()) {
			builder.append(comparison.getName());
			builder.append("=");
			builder.append(comparison.getValue());
			return;
		}
		
		if (comparison.getValue() != null) {
			final String sOperator = comparison.getOperator().getOperator();
            if (!comparison.isCaseSensitive()) {
                 builder.append(" UPPER( ");
            }
			if (!comparison.isAlias()) {
				builder.append(" o.");
			} else {
				builder.append(" ");
			}
			builder.append(comparison.getName());
            if (!comparison.isCaseSensitive()) {
                 builder.append(" ) ");
            }


			builder.append(" ");
			builder.append(sOperator);
			builder.append(" ");

            if (!comparison.isCaseSensitive()) {
                 builder.append(" UPPER( ");
            }
			if (comparison instanceof Between
					|| comparison instanceof VerifiedBetween) {
				builder.append(var).append("_1");
				builder.append(" ");
				builder.append("and ").append(var).append("_2");
			} else if (comparison.getOperator() == Operator.IN) {
				builder.append(" (");
				builder.append(var);
				builder.append(") ");
			} else {
				builder.append(var);
			}
			builder.append(" ");
            if (!comparison.isCaseSensitive()) {
                 builder.append(" ) ");
            }
            
		} else {
			if (!comparison.isAlias()) {
				builder.append(" o.");
			} else {
				builder.append(" ");
			}
			builder.append(comparison.getName());
			if (comparison.getOperator() == Operator.EQ) {
				builder.append(" is null ");
			} else if (comparison.getOperator() == Operator.NE) {
				builder.append(" is not null ");
			}
		}
	}


	public static String constructOrderBy(OrderBy[] orderBys) {
		StringBuilder query = new StringBuilder(100);
		if (null != orderBys && orderBys.length > 0) {
			query.append(" ORDER BY ");

			for (int index = 0; index < orderBys.length; index++) {

                OrderBy cob = orderBys[index];
                if (cob == null) {
                    throw new IllegalStateException();
                }
                if (!cob.isCaseSensitive()){
                    query.append("UPPER( " );
                }
				if (!cob.isAlias()) {
					query.append("o." + cob.getName());
				} else {
					query.append(cob.getName());
				}
				query.append(" ");
                if (!cob.isCaseSensitive()){
                    query.append(") ");
                }
				query.append(cob.getDirection().toString());
				if (index + 1 < orderBys.length) {
					query.append(", ");
				}
			}
		}
		return query.toString();
	}

	public static String constructSelect(Select[] selects, String newSelectStatement, String entityName, String instanceName, boolean distinctFlag) {
		StringBuilder query = new StringBuilder(255);
		query.append("SELECT ");

		if (newSelectStatement != null) {
			query.append(newSelectStatement);
			return query.toString();
		}			
		query.append((distinctFlag ? " DISTINCT " : " "));
		query.append(instanceName);
		
		if (selects == null || selects.length == 0) {
			return query.toString();
		} else {
			query.append(", ");
			for (Select select : selects) {
				query.append((select.isDistinct() ? " DISTINCT " : " "));
				query.append(select.getName());
				query.append(", ");
			}
			return query.substring(0, query.length()-2);			
		}		
	}
	

	public static String constructJoins(Join[] joins) {
		if (joins == null || joins.length == 0) {
			return "";
		}
		StringBuilder builder = new StringBuilder(255);
		for (Join join : joins) {
			if (join instanceof RelationshipJoin) {
				RelationshipJoin rj = (RelationshipJoin) join;

				
				if (rj.getJoinType() == JoinType.LEFT) {
					builder.append(" left ");
				}
				
				if (rj instanceof RelationshipFetch) {
					builder.append(" join fetch ");
				} else if (rj instanceof SimpleRelationshipJoin) {
					builder.append(" join ");
				}

				builder.append(rj.isAliasedRelationship() ? "" : "o.");
				builder.append(rj.getRelationshipProperty());
				builder.append(" ");
				builder.append(rj.getAlias().equals("") ? rj.getDefaultAlias()
						: rj.getAlias());
			}
		}

		return builder.toString();
	}

	public static String constructFrom(Class <?> type, Join... joins) {
		final StringBuilder sbquery = new StringBuilder(35);
		sbquery.append(" FROM ");
		sbquery.append(GenericDaoUtils.getEntityName(type));
		sbquery.append(" o ");
		if (joins == null || joins.length == 0) {
			return sbquery.toString();
		}
		for (Join join : joins){
			if (join instanceof EntityJoin) {
				EntityJoin ej = (EntityJoin) join;
				sbquery.append(" , ");
				sbquery.append(ej.getName());
				sbquery.append(" ");
				sbquery.append(ej.getAlias());
			}
		}
		return sbquery.toString();
	}
	
	public static String ensureUnique(Set<String> names, String name) {
		if (names.contains(name)) {
			int index = 0;
			String tempVar = null;
			while (true) {
				tempVar = name + "_" + index;
				if (!names.contains(tempVar)) {
					break;
				}
				index++;
			}
			name = tempVar;
		}
		names.add(name);
		return name;
	}
	

	public static String ditchDot(String propName) {
		propName = propName.replace('.', '_');
		return propName;
	}	
	
	
	public static String createQuery(Class<?> clazz, Select[] selects, String newSelectStatement,
			boolean distinctFlag, OrderBy[] orderBy, Join[] joins,
			final Group group) {
		StringBuilder sbQuery = new StringBuilder(255);
		final String sQuery = sbQuery
				.append(	CriteriaUtils.constructSelect(selects, newSelectStatement, GenericDaoUtils.getEntityName(clazz), "o", distinctFlag))
				.append(    CriteriaUtils.constructFrom(clazz, joins) )
				.append(    CriteriaUtils.constructJoins(joins)).append(CriteriaUtils.constuctWhereClause(group))
				.append(	CriteriaUtils.constructOrderBy(orderBy)).toString();
		logger.debug(String.format("Query %s ", sQuery));
		return sQuery;
	}

	public static String createCountQuery(final Group group, Class<?> type, boolean distinct) {
		final StringBuilder sbquery = new StringBuilder("SELECT count("
				+ (distinct ? "DISTINCT " : "") + "o ) " + " FROM ");
		sbquery.append(GenericDaoUtils.getEntityName(type));
		sbquery.append(" ");
		sbquery.append("o").append(" ").append(CriteriaUtils.constuctWhereClause(group));
		return sbquery.toString();
	}
}
