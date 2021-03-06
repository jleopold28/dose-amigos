package info.doseamigos.meds;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;

import info.doseamigos.db.MySQLConnection;

/**
 * MySQL implementation of the {@link MedDao}.
 */
public class MySQLMedDao implements MedDao {
    @Override
    public Long save(Med med) throws SQLException {
        Connection conn = null;
        Long toReturn;
        try {
            conn = MySQLConnection.create();
            conn.setAutoCommit(false);
            if (med.getMedId() != null) {
                PreparedStatement updateStatement = conn.prepareStatement(
                    "UPDATE MEDS set name = ?, doseamount = ?, doseUnit = ?, totalAmount = ? " +
                        "WHERE medId = ?"
                );
                updateStatement.setString(1, med.getName());
                updateStatement.setInt(2, med.getDoseAmount());
                updateStatement.setString(3, med.getDoseUnit());
                updateStatement.setInt(4, med.getTotalAmount());
                updateStatement.setLong(5, med.getMedId());
                updateStatement.executeUpdate();

                toReturn = med.getMedId();
            } else {
                PreparedStatement insertStatement = conn.prepareStatement(
                    "INSERT INTO MEDS(amigouserid, rxcui, name, doseamount, doseUnit, totalAmount, doseInstructions, firstTaken, lastDoseTaken, active) " +
                        "VALUES (?,?,?,?,?,?,?,?,?,'Y')"
                );
                insertStatement.setLong(1, med.getUser().getId());
                if (med.getRxcui() != null) {
                    insertStatement.setLong(2, med.getRxcui());
                } else {
                    insertStatement.setNull(2, Types.INTEGER);
                }
                insertStatement.setString(3, med.getName());
                insertStatement.setInt(4, med.getDoseAmount());
                insertStatement.setString(5, med.getDoseUnit());
                insertStatement.setInt(6, med.getTotalAmount());
                insertStatement.setString(7, med.getDoseInstructions());
                insertStatement.setDate(8, med.getFirstTaken() != null
                    ? new Date(med.getFirstTaken().getTime())
                    : new Date(new java.util.Date().getTime()));
                insertStatement.setDate(9, med.getLastTaken() != null
                    ? new Date(med.getLastTaken().getTime())
                    : null);

                insertStatement.executeUpdate();

                PreparedStatement getNewId = conn.prepareStatement(
                    "SELECT LAST_INSERT_ID() AS newMedId"
                );

                ResultSet rs = getNewId.executeQuery();
                rs.next();
                toReturn = rs.getLong("newMedId");
            }
            conn.commit();
        } catch (SQLException e) {
            throw e;
        } finally {
            conn.rollback();
            conn.close();
        }
        return toReturn;
    }

    @Override
    public Med getById(Long id) {
        try (Connection conn = MySQLConnection.create()) {
            PreparedStatement statement = conn.prepareStatement(
                "   SELECT MEDS.medId," +
                    "      MEDS.rxcui," +
                    "      MEDS.name AS medName," +
                    "      MEDS.doseamount," +
                    "      MEDS.doseUnit," +
                    "      MEDS.totalAmount," +
                    "      MEDS.doseInstructions," +
                    "      MEDS.firstTaken," +
                    "      MEDS.lastDoseTaken," +
                    "      MEDS.nextScheduledDose," +
                    "      MEDS.active," +
                    "      AMIGOUSERS.amigouserid," +
                    "      AMIGOUSERS.picUrl, " +
                    "      AMIGOUSERS.lastTimeDoseTaken," +
                    "      AMIGOUSERS.nextTimeDoseScheduled," +
                    "      AMIGOUSERS.name AS amigoName" +
                    " FROM MEDS" +
                    " JOIN AMIGOUSERS" +
                    "   ON MEDS.amigoUserId = AMIGOUSERS.amigoUserId " +
                    "WHERE MEDS.medId = ? "
            );
            statement.setLong(1, id);
            ResultSet rs = statement.executeQuery();
            if (!rs.next()) {
                return null;
            }
            return new MedRowMapper().mapRow(rs);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public List<Med> getMedsForAmigo(Long amigoId) {
        List<Med> meds = new ArrayList<>();
        try (Connection conn = MySQLConnection.create()) {
            PreparedStatement statement = conn.prepareStatement(
                "   SELECT MEDS.medId," +
                    "      MEDS.rxcui," +
                    "      MEDS.name AS medName," +
                    "      MEDS.doseamount," +
                    "      MEDS.doseUnit," +
                    "      MEDS.totalAmount," +
                    "      MEDS.doseInstructions," +
                    "      MEDS.firstTaken," +
                    "      MEDS.lastDoseTaken," +
                    "      MEDS.nextScheduledDose," +
                    "      MEDS.active," +
                    "      AMIGOUSERS.amigouserid," +
                    "      AMIGOUSERS.picUrl, " +
                    "      AMIGOUSERS.lastTimeDoseTaken," +
                    "      AMIGOUSERS.nextTimeDoseScheduled," +
                    "      AMIGOUSERS.name AS amigoName" +
                    " FROM MEDS" +
                    " JOIN AMIGOUSERS" +
                    "   ON MEDS.amigoUserId = AMIGOUSERS.amigoUserId " +
                    "WHERE MEDS.amigoUserId = ? " +
                    "  AND MEDS.active = 'Y'"
            );
            statement.setLong(1, amigoId);
            ResultSet rs = statement.executeQuery();
            while (rs.next()) {
                meds.add(new MedRowMapper().mapRow(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        return meds;
    }
}
