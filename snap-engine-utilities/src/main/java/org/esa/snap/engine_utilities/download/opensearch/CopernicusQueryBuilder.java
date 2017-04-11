/*
 * Copyright (C) 2017 by Array Systems Computing Inc. http://www.array.ca
 *
 * This program is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License as published by the Free
 * Software Foundation; either version 3 of the License, or (at your option)
 * any later version.
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for
 * more details.
 *
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, see http://www.gnu.org/licenses/
 */
package org.esa.snap.engine_utilities.download.opensearch;

import org.esa.snap.core.util.StringUtils;
import org.esa.snap.engine_utilities.db.DBQuery;

import java.awt.*;
import java.util.Calendar;

/**
 * helps create queries for SciHub
 */
public class CopernicusQueryBuilder {
    private final DBQuery dbQuery;

    private static final String COPERNICUS_HOST = "https://scihub.copernicus.eu";
    //private static final String SEARCH_ROOT = "/dhus/search?q=";
    private static final String SEARCH_ROOT = "/apihub/search?q=";

    //private static final String DATE = "( beginPosition:[2016-01-25T00:00:00.000Z TO 2016-01-25T23:59:59.999Z] AND endPosition:[2016-01-25T00:00:00.000Z TO 2016-01-25T23:59:59.999Z] )";
    //private static final String FOOTPRINT = "( footprint:\"Intersects(POLYGON((-74.24323771090575 -34.81331346157173,-31.2668365052604 -34.81331346157173,-31.2668365052604 5.647318588641241,-74.24323771090575 5.647318588641241,-74.24323771090575 -34.81331346157173)))\" )";

    //private static final String searchURL = COPERNICUS_HOST + SEARCH_ROOT + "( " + FOOTPRINT + " AND " + DATE + " AND (platformname:Sentinel-1 AND producttype:GRD)";


    public CopernicusQueryBuilder(final DBQuery dbQuery) {
        this.dbQuery = dbQuery;
    }

    public String getSearchURL() {
        final StringBuilder str = new StringBuilder();
        str.append("( ");

        str.append(getFootprint());
        //str.append(FOOTPRINT);
        str.append(getDate());

        str.append(getMission());
        str.append(getProductType());
        str.append(getSensorMode());
        str.append(getProductName());
        str.append(getOrbitDirection());

        str.append(" )");

        return COPERNICUS_HOST + SEARCH_ROOT + str.toString();
    }

    private String getFootprint() {
        final Rectangle.Double rect = dbQuery.getSelectionRectangle();
        if(rect != null && rect.width != 0 && rect.height != 0) {
            final StringBuilder str = new StringBuilder();
            str.append("( footprint:\"Intersects(POLYGON((");

            str.append(rect.y); // lat
            str.append(' ');
            str.append(rect.x); // lon
            str.append(", ");

            str.append(rect.y);
            str.append(' ');
            str.append(rect.x  + rect.width);
            str.append(", ");

            str.append(rect.y + rect.height);
            str.append(' ');
            str.append(rect.x + rect.width);
            str.append(", ");

            str.append(rect.y + rect.height);
            str.append(' ');
            str.append(rect.x);
            str.append(", ");

            str.append(rect.y);
            str.append(' ');
            str.append(rect.x);

            str.append(")))\" )");
            return str.toString();
        } else if(rect != null) {
            final StringBuilder str = new StringBuilder();
            str.append("( footprint:\"Intersects(");

            str.append(rect.y); // lat
            str.append(", ");
            str.append(rect.x); // lon

            str.append(")\" )");
            return str.toString();
        }
        return "";
    }

    private String getDate() {
        final Calendar start = dbQuery.getStartDate();
        final Calendar end = dbQuery.getEndDate();

        if(start != null || end != null) {
            final StringBuilder str = new StringBuilder();
            str.append(" AND ( ");

            if(start != null) {
                str.append("beginPosition:[");

                int year = start.get(Calendar.YEAR);
                int month = start.get(Calendar.MONTH) + 1;
                int day = start.get(Calendar.DAY_OF_MONTH);

                str.append(String.valueOf(year) + '-' + month + '-' + day);
                str.append("T00:00:00.000Z TO ");

                if(end != null) {

                    int year2 = end.get(Calendar.YEAR);
                    int month2 = end.get(Calendar.MONTH) + 1;
                    int day2 = end.get(Calendar.DAY_OF_MONTH);

                    str.append(String.valueOf(year2) + '-' + month2 + '-' + day2);
                    str.append("T00:00:00.000Z");
                } else {
                    str.append("NOW");
                }

                str.append("] ");
            } else {
                str.append("beginPosition:[");

                str.append("2014-01-01T00:00:00.000Z TO ");

                int year2 = end.get(Calendar.YEAR);
                int month2 = end.get(Calendar.MONTH) + 1;
                int day2 = end.get(Calendar.DAY_OF_MONTH);

                str.append(String.valueOf(year2) + '-' + month2 + '-' + day2);
                str.append("T00:00:00.000Z");

                str.append("] ");
            }

            str.append(" )");
            return str.toString();
        }
        return "";
    }

    private String getMission() {
        final String[] missions = dbQuery.getSelectedMissions();
        if (missions != null && missions.length > 0 && !StringUtils.contains(missions, DBQuery.ALL_MISSIONS)) {
            final StringBuilder str = new StringBuilder();
            str.append(" AND (");

            for (int i = 0; i < missions.length; ++i) {
                if(i > 0) {
                    str.append(" OR ");
                }
                str.append("platformname:" + missions[i]);
            }
            str.append(" )");

            return str.toString();
        }
        return "";
    }

    private String getProductType() {
        final String[] productTypes = dbQuery.getSelectedProductTypes();
        if (productTypes != null && productTypes.length > 0 && !StringUtils.contains(productTypes, DBQuery.ALL_PRODUCT_TYPES)) {
            final StringBuilder str = new StringBuilder();
            str.append(" AND (");

            for (int i = 0; i < productTypes.length; ++i) {
                if(i > 0) {
                    str.append(" OR ");
                }
                str.append("producttype:" + productTypes[i]);
            }
            str.append(" )");

            return str.toString();
        }
        return "";
    }

    private String getProductName() {
        final String name = dbQuery.getSelectedName();
        if (name != null && !name.isEmpty()) {
            String str = " AND (" +
                    "filename:*" + name + "* " +
                    " )";

            return str;
        }
        return "";
    }

    private String getSensorMode() {
        final String mode = dbQuery.getSelectedAcquisitionMode();
        if (mode != null && !mode.isEmpty() && !mode.equals(DBQuery.ALL_MODES)) {
            String str = " AND (" +
                    "sensoroperationalmode:" + mode +
                    " )";

            return str;
        }
        return "";
    }

    private String getOrbitDirection() {
        final String pass = dbQuery.getSelectedPass();
        if (pass != null && !pass.isEmpty() && !pass.equals(DBQuery.ALL_PASSES)) {
            String str = " AND (" +
                    "orbitdirection:" + pass +
                    " )";

            return str;
        }
        return "";
    }
}
