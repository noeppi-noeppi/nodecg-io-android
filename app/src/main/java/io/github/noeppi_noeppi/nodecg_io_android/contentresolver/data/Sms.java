package io.github.noeppi_noeppi.nodecg_io_android.contentresolver.data;

import android.provider.Telephony;
import io.github.noeppi_noeppi.nodecg_io_android.contentresolver.mapping.DataClass;
import io.github.noeppi_noeppi.nodecg_io_android.contentresolver.mapping.Mapping;

@DataClass
public class Sms {

    @Mapping(Telephony.TextBasedSmsColumns.ADDRESS)
    public String address;

    @Mapping(Telephony.TextBasedSmsColumns.BODY)
    public String body;

    @Mapping(Telephony.TextBasedSmsColumns.DATE)
    public long receivedDate;

    @Mapping(Telephony.TextBasedSmsColumns.DATE_SENT)
    public long sentDate;
}
