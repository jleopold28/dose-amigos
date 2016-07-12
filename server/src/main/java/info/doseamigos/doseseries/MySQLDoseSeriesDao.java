package info.doseamigos.doseseries;

import info.doseamigos.db.MySQLConnection;
import info.doseamigos.meds.Med;
import info.doseamigos.meds.MedRowMapper;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;

/**
 * MySQL implementation for {@link DoseSeriesDao}.
 */
public class MySQLDoseSeriesDao implements DoseSeriesDao {

    @Override
    public Long save(DoseSeries series) throws SQLException {

        Connection conn = null;
        Long toRet = null;
        try {
            conn = MySQLConnection.create();
            conn.setAutoCommit(false);

            //First, create the series row if now seriesId exists
            Long seriesId = series.getSeriesId();
            if (seriesId == null) {
                PreparedStatement seriesRowStatement = conn.prepareStatement(
                    "INSERT INTO DOSESERIES(medId) VALUES (?)"
                );
                seriesRowStatement.setLong(1, series.getMed().getMedId());
                seriesRowStatement.executeUpdate();
                PreparedStatement getSeriesId = conn.prepareStatement(
                    "SELECT LAST_INSERT_ID() AS seriesId"
                );
                ResultSet seriesIdRS = getSeriesId.executeQuery();
                seriesIdRS.next();
                seriesId = seriesIdRS.getLong("seriesId");

            }

            //Now delete all existing items with the series id
            PreparedStatement deleteAllExistingItems = conn.prepareStatement(
                "DELETE FROM DOSESERIESITEM WHERE seriesId = ?"
            );
            deleteAllExistingItems.setLong(1, seriesId);
            deleteAllExistingItems.executeUpdate();

            //Now add each item.
            for (Integer day : series.getDays()) {
                for (Date time : series.getTimes()) {
                    PreparedStatement addItem = conn.prepareStatement(
                        "INSERT INTO DOSESERIESITEM(seriesId, seriesDay, seriesTime) " +
                            "VALUES (?,?,?)"
                    );
                    addItem.setLong(1, seriesId);
                    addItem.setInt(2, day);
                    addItem.setTimestamp(3, new java.sql.Timestamp(time.getTime()));

                    addItem.executeUpdate();
                }
            }
            conn.commit();
            toRet = seriesId;

        } catch (SQLException e) {
            throw e;
        } finally {
            conn.rollback();
            conn.close();
        }

        return toRet;
    }

    @Override
    public DoseSeries getById(Long id) {
        DoseSeries series = new DoseSeries();
        series.setSeriesId(id);
        series.setDays(new ArrayList<Integer>());
        series.setTimes(new ArrayList<Date>());
        try (Connection conn = MySQLConnection.create()) {
            PreparedStatement statement = conn.prepareStatement(
                "SELECT DOSESERIESITEM.seriesId, " +
                    "   DOSESERIESITEM.seriesDay, " +
                    "   DOSESERIESITEM.seriesTime, " +
                    "   MEDS.medId," +
                    "   MEDS.rxcui," +
                    "   MEDS.name AS medName," +
                    "   MEDS.doseamount," +
                    "   MEDS.doseUnit," +
                    "   MEDS.totalAmount," +
                    "   MEDS.doseInstructions," +
                    "   MEDS.firstTaken," +
                    "   MEDS.lastDoseTaken," +
                    "   MEDS.nextScheduledDose," +
                    "   MEDS.active," +
                    "   AMIGOUSERS.amigouserid," +
                    "   AMIGOUSERS.name AS amigoName" +
                    " FROM DOSESERIESITEM " +
                    " JOIN DOSESERIES " +
                    "   ON DOSESERIESITEM.seriesId = DOSESERIES.seriesId" +
                    " JOIN MEDS " +
                    "   ON DOSESERIES.medId = MEDS.medId " +
                    " JOIN AMIGOUSERS " +
                    "   ON MEDS.amigouserid = AMIGOUSERS.amigouserid " +
                    "WHERE DOSESERIES.seriesId = ?"
            );
            statement.setLong(1, id);
            ResultSet rs = statement.executeQuery();
            Med med = null;
            while (rs.next()) {
                if (med == null) {
                    med = new MedRowMapper().mapRow(rs);
                }
                Integer seriesDay = rs.getInt("seriesDay");
                Date seriesTime = new Date(rs.getTimestamp("seriesTime").getTime());
                if (!series.getDays().contains(seriesDay)) {
                    series.getDays().add(seriesDay);
                }
                if (!series.getTimes().contains(seriesTime)) {
                    series.getTimes().add(seriesTime);
                }
            }
            series.setMed(med);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        return series;
    }
}