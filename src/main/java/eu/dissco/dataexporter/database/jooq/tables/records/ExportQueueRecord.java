/*
 * This file is generated by jOOQ.
 */
package eu.dissco.dataexporter.database.jooq.tables.records;


import eu.dissco.dataexporter.database.jooq.enums.ExportType;
import eu.dissco.dataexporter.database.jooq.enums.JobState;
import eu.dissco.dataexporter.database.jooq.tables.ExportQueue;

import java.time.Instant;
import java.util.UUID;

import org.jooq.JSONB;
import org.jooq.Record1;
import org.jooq.impl.UpdatableRecordImpl;


/**
 * This class is generated by jOOQ.
 */
@SuppressWarnings({ "all", "unchecked", "rawtypes", "this-escape" })
public class ExportQueueRecord extends UpdatableRecordImpl<ExportQueueRecord> {

    private static final long serialVersionUID = 1L;

    /**
     * Setter for <code>public.export_queue.id</code>.
     */
    public void setId(UUID value) {
        set(0, value);
    }

    /**
     * Getter for <code>public.export_queue.id</code>.
     */
    public UUID getId() {
        return (UUID) get(0);
    }

    /**
     * Setter for <code>public.export_queue.params</code>.
     */
    public void setParams(JSONB value) {
        set(1, value);
    }

    /**
     * Getter for <code>public.export_queue.params</code>.
     */
    public JSONB getParams() {
        return (JSONB) get(1);
    }

    /**
     * Setter for <code>public.export_queue.creator</code>.
     */
    public void setCreator(String value) {
        set(2, value);
    }

    /**
     * Getter for <code>public.export_queue.creator</code>.
     */
    public String getCreator() {
        return (String) get(2);
    }

    /**
     * Setter for <code>public.export_queue.job_state</code>.
     */
    public void setJobState(JobState value) {
        set(3, value);
    }

    /**
     * Getter for <code>public.export_queue.job_state</code>.
     */
    public JobState getJobState() {
        return (JobState) get(3);
    }

    /**
     * Setter for <code>public.export_queue.time_scheduled</code>.
     */
    public void setTimeScheduled(Instant value) {
        set(4, value);
    }

    /**
     * Getter for <code>public.export_queue.time_scheduled</code>.
     */
    public Instant getTimeScheduled() {
        return (Instant) get(4);
    }

    /**
     * Setter for <code>public.export_queue.time_started</code>.
     */
    public void setTimeStarted(Instant value) {
        set(5, value);
    }

    /**
     * Getter for <code>public.export_queue.time_started</code>.
     */
    public Instant getTimeStarted() {
        return (Instant) get(5);
    }

    /**
     * Setter for <code>public.export_queue.time_completed</code>.
     */
    public void setTimeCompleted(Instant value) {
        set(6, value);
    }

    /**
     * Getter for <code>public.export_queue.time_completed</code>.
     */
    public Instant getTimeCompleted() {
        return (Instant) get(6);
    }

    /**
     * Setter for <code>public.export_queue.export_type</code>.
     */
    public void setExportType(ExportType value) {
        set(7, value);
    }

    /**
     * Getter for <code>public.export_queue.export_type</code>.
     */
    public ExportType getExportType() {
        return (ExportType) get(7);
    }

    /**
     * Setter for <code>public.export_queue.hashed_params</code>.
     */
    public void setHashedParams(UUID value) {
        set(8, value);
    }

    /**
     * Getter for <code>public.export_queue.hashed_params</code>.
     */
    public UUID getHashedParams() {
        return (UUID) get(8);
    }

    /**
     * Setter for <code>public.export_queue.destination_email</code>.
     */
    public void setDestinationEmail(String value) {
        set(9, value);
    }

    /**
     * Getter for <code>public.export_queue.destination_email</code>.
     */
    public String getDestinationEmail() {
        return (String) get(9);
    }

    /**
     * Setter for <code>public.export_queue.download_link</code>.
     */
    public void setDownloadLink(String value) {
        set(10, value);
    }

    /**
     * Getter for <code>public.export_queue.download_link</code>.
     */
    public String getDownloadLink() {
        return (String) get(10);
    }

    // -------------------------------------------------------------------------
    // Primary key information
    // -------------------------------------------------------------------------

    @Override
    public Record1<UUID> key() {
        return (Record1) super.key();
    }

    // -------------------------------------------------------------------------
    // Constructors
    // -------------------------------------------------------------------------

    /**
     * Create a detached ExportQueueRecord
     */
    public ExportQueueRecord() {
        super(ExportQueue.EXPORT_QUEUE);
    }

    /**
     * Create a detached, initialised ExportQueueRecord
     */
    public ExportQueueRecord(UUID id, JSONB params, String creator, JobState jobState, Instant timeScheduled, Instant timeStarted, Instant timeCompleted, ExportType exportType, UUID hashedParams, String destinationEmail, String downloadLink) {
        super(ExportQueue.EXPORT_QUEUE);

        setId(id);
        setParams(params);
        setCreator(creator);
        setJobState(jobState);
        setTimeScheduled(timeScheduled);
        setTimeStarted(timeStarted);
        setTimeCompleted(timeCompleted);
        setExportType(exportType);
        setHashedParams(hashedParams);
        setDestinationEmail(destinationEmail);
        setDownloadLink(downloadLink);
        resetChangedOnNotNull();
    }
}
