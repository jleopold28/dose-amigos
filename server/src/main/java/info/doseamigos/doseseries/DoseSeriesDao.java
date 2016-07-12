package info.doseamigos.doseseries;

import java.sql.SQLException;

/**
 * DAO for handling saving and grabbing DoseSeries for Meds.
 */
public interface DoseSeriesDao {

    /**
     * Saves a doseseries to the DB.
     * @param series The series to save
     * @return The id of the series saved.
     */
    Long save(DoseSeries series) throws SQLException;

    /**
     * Gets a series by its id.
     * @param id The id to grab the series for.
     * @return The series with the given id.
     */
    DoseSeries getById(Long id);
}