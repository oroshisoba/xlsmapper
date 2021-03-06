package com.gh.mygreen.xlsmapper.cellconvert;

import static com.gh.mygreen.xlsmapper.TestUtils.*;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

import java.awt.Point;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.Test;

import com.gh.mygreen.xlsmapper.IsEmptyBuilder;
import com.gh.mygreen.xlsmapper.XlsMapper;
import com.gh.mygreen.xlsmapper.annotation.OverRecordOperate;
import com.gh.mygreen.xlsmapper.annotation.RecordTerminal;
import com.gh.mygreen.xlsmapper.annotation.XlsArrayConverter;
import com.gh.mygreen.xlsmapper.annotation.XlsColumn;
import com.gh.mygreen.xlsmapper.annotation.XlsConverter;
import com.gh.mygreen.xlsmapper.annotation.XlsHint;
import com.gh.mygreen.xlsmapper.annotation.XlsHorizontalRecords;
import com.gh.mygreen.xlsmapper.annotation.XlsIsEmpty;
import com.gh.mygreen.xlsmapper.annotation.XlsSheet;
import com.gh.mygreen.xlsmapper.validation.SheetBindingErrors;

/**
 * リスト/集合/配列型の
 * 
 * @version 1.1
 * @since 0.5
 * @author T.TSUCHIE
 *
 */
public class CollectionCellConveterTest {
    
    @AfterClass
    public static void tearDownAfterClass() throws Exception {
    }
    
    @Before
    public void setUp() throws Exception {
    }
    
    @After
    public void tearDown() throws Exception {
    }
    
    /**
     * リスト、集合、配列型の読み込みテスト
     */
    @Test
    public void test_load_collection() throws Exception {
        
        XlsMapper mapper = new XlsMapper();
        mapper.getConig().setContinueTypeBindFailure(true);
        
        try(InputStream in = new FileInputStream("src/test/data/convert.xlsx")) {
            SheetBindingErrors errors = new SheetBindingErrors(CollectionSheet.class);
            
            CollectionSheet sheet = mapper.load(in, CollectionSheet.class, errors);
            
            if(sheet.simpleRecords != null) {
                for(SimpleRecord record : sheet.simpleRecords) {
                    assertRecord(record, errors);
                }
            }
            
            if(sheet.formattedRecords != null) {
                for(FormattedRecord record : sheet.formattedRecords) {
                    assertRecord(record, errors);
                }
            }
            
            if(sheet.customRecords != null) {
                for(CustomRecord record : sheet.customRecords) {
                    assertRecord(record, errors);
                }
            }
            
        }
    }
    
    private void assertRecord(final SimpleRecord record, final SheetBindingErrors errors) {
        
        if(record.no == 1) {
            // 空文字
            assertThat(record.listText, empty());
            assertThat(record.listInteger, empty());
            
            assertThat(record.arrayText, emptyArray());
            assertThat(record.arrayInteger, emptyArray());
            
            assertThat(record.setText, empty());
            assertThat(record.setInteger, empty());
            
        } else if(record.no == 2) {
            // 項目が１つ
            assertThat(record.listText, contains("abc"));
            assertThat(record.listInteger, contains(123));
            
            assertThat(record.arrayText, arrayContaining("abc"));
            assertThat(record.arrayInteger, arrayContaining(123));
            
            assertThat(record.setText, contains("abc"));
            assertThat(record.setInteger, contains(123));
            
        } else if(record.no == 3) {
            // 項目が2つ
            assertThat(record.listText, contains("abc", "def"));
            assertThat(record.listInteger, contains(123, 456));
            
            assertThat(record.arrayText, arrayContaining("abc", "def"));
            assertThat(record.arrayInteger, arrayContaining(123, 456));
            
            assertThat(record.setText, contains("abc", "def"));
            assertThat(record.setInteger, contains(123, 456));
            
        } else if(record.no == 4) {
            // 区切り文字のみ
            assertThat(record.listText, empty());
            assertThat(record.listInteger, empty());
            
            assertThat(record.arrayText, emptyArray());
            assertThat(record.arrayInteger, emptyArray());
            
            assertThat(record.setText, empty());
            assertThat(record.setInteger, empty());
            
        } else if(record.no == 5) {
            // 空の項目がある
            assertThat(record.listText, contains("abc", null, "def"));
            assertThat(record.listInteger, contains(123, null, 456));
            
            assertThat(record.arrayText, arrayContaining("abc", null, "def"));
            assertThat(record.arrayInteger, arrayContaining(123,null,  456));
            
            assertThat(record.setText, contains("abc", null, "def"));
            assertThat(record.setInteger, contains(123, null, 456));
            
        } else if(record.no == 6) {
            // 空の項目がある
            assertThat(record.listText, contains("  abc", " def "));
            assertThat(cellFieldError(errors, cellAddress(record.positions.get("listInteger"))).isTypeBindFailure(), is(true));
            
            assertThat(record.arrayText, arrayContaining("  abc", " def "));
            assertThat(cellFieldError(errors, cellAddress(record.positions.get("arrayInteger"))).isTypeBindFailure(), is(true));
            
            assertThat(record.setText, contains("  abc", " def "));
            assertThat(cellFieldError(errors, cellAddress(record.positions.get("setInteger"))).isTypeBindFailure(), is(true));
            
        } else {
            fail(String.format("not support test case. No=%d.", record.no));
        }
    }
    
    private void assertRecord(final FormattedRecord record, final SheetBindingErrors errors) {
        
        if(record.no == 1) {
            // 空文字
            assertThat(record.listText, empty());
            assertThat(record.listInteger, contains(0));
            
            assertThat(record.arrayText, emptyArray());
            assertThat(record.arrayInteger, arrayContaining(0));
            
            assertThat(record.setText, empty());
            assertThat(record.setInteger, contains(0));
            
        } else if(record.no == 2) {
            // 項目が１つ
            assertThat(record.listText, contains("abc"));
            assertThat(record.listInteger, contains(123));
            
            assertThat(record.arrayText, arrayContaining("abc"));
            assertThat(record.arrayInteger, arrayContaining(123));
            
            assertThat(record.setText, contains("abc"));
            assertThat(record.setInteger, contains(123));
            
        } else if(record.no == 3) {
            // 項目が2つ
            assertThat(record.listText, contains("abc", "def"));
            assertThat(record.listInteger, contains(123, 456));
            
            assertThat(record.arrayText, arrayContaining("abc", "def"));
            assertThat(record.arrayInteger, arrayContaining(123, 456));
            
            assertThat(record.setText, contains("abc", "def"));
            assertThat(record.setInteger, contains(123, 456));
            
        } else if(record.no == 4) {
            // 区切り文字のみ
            assertThat(record.listText, empty());
            assertThat(record.listInteger, empty());
            
            assertThat(record.arrayText, emptyArray());
            assertThat(record.arrayInteger, emptyArray());
            
            assertThat(record.setText, empty());
            assertThat(record.setInteger, empty());
            
        } else if(record.no == 5) {
            // 区切り文字、空白
            assertThat(record.listText, contains("abc", "def"));
            assertThat(record.listInteger, contains(123, 456));
            
            assertThat(record.arrayText, arrayContaining("abc", "def"));
            assertThat(record.arrayInteger, arrayContaining(123, 456));
            
            assertThat(record.setText, contains("abc", "def"));
            assertThat(record.setInteger, contains(123, 456));
            
        } else if(record.no == 6) {
            // 空白がある
            assertThat(record.listText, contains("abc", "def"));
            assertThat(record.listInteger, contains(123, 456));
            
            assertThat(record.arrayText, arrayContaining("abc", "def"));
            assertThat(record.arrayInteger, arrayContaining(123, 456));
            
            assertThat(record.setText, contains("abc", "def"));
            assertThat(record.setInteger, contains(123, 456));
            
        } else {
            fail(String.format("not support test case. No=%d.", record.no));
        }
        
    }
    
    private void assertRecord(final CustomRecord record, final SheetBindingErrors errors) {
        
        if(record.no == 1) {
            // 空文字
            assertThat(record.listDate, empty());
            assertThat(record.arrayDate, emptyArray());
            assertThat(record.setDate, empty());
            
        } else if(record.no == 2) {
            // 項目が１つ
            Date date1 = toUtilDate(toTimestamp("2016-03-15 00:00:00.000"));
            
            assertThat(record.listDate, contains(date1));
            assertThat(record.arrayDate, arrayContaining(date1));
            assertThat(record.setDate, contains(date1));
            
        } else if(record.no == 3) {
            // 項目が2つ
            
            Date date1 = toUtilDate(toTimestamp("2016-03-15 00:00:00.000"));
            Date date2 = toUtilDate(toTimestamp("2016-03-16 00:00:00.000"));
            
            assertThat(record.listDate, contains(date1, date2));
            assertThat(record.arrayDate, arrayContaining(date1, date2));
            assertThat(record.setDate, contains(date1, date2));
            
        } else if(record.no == 4) {
            // 区切り文字のみ
            assertThat(record.listDate, empty());
            assertThat(record.arrayDate, emptyArray());
            assertThat(record.setDate, empty());
            
        } else if(record.no == 5) {
            // 区切り文字、空白
            
            Date date1 = toUtilDate(toTimestamp("2016-03-15 00:00:00.000"));
            Date date2 = toUtilDate(toTimestamp("2016-03-16 00:00:00.000"));
            
            assertThat(record.listDate, contains(date1, date2));
            assertThat(record.arrayDate, arrayContaining(date1, date2));
            assertThat(record.setDate, contains(date1, date2));
            
        } else if(record.no == 6) {
            // 空白がある
            
            Date date1 = toUtilDate(toTimestamp("2016-03-15 00:00:00.000"));
            Date date2 = toUtilDate(toTimestamp("2016-03-16 00:00:00.000"));
            
            assertThat(record.listDate, contains(date1, date2));
            assertThat(record.arrayDate, arrayContaining(date1, date2));
            assertThat(record.setDate, contains(date1, date2));
            
        } else {
            fail(String.format("not support test case. No=%d.", record.no));
        }
        
    }
    
    /**
     * リスト、集合、配列型の書き込みテスト
     */
    @Test
    public void test_save_collection() throws Exception {
        
        // テストデータの作成
        final CollectionSheet outSheet = new CollectionSheet();
        
        // アノテーションなしのデータ作成
        outSheet.add(new SimpleRecord()
                .comment("空文字"));
        
        outSheet.add(new SimpleRecord()
                .listText(toList("abc"))
                .listInteger(toList(123))
                .arrayText(toArray("abc"))
                .arrayInteger(toArray(123))
                .setText(toSet("abc"))
                .setInteger(toSet(123))
                .comment("項目が1つ"));
        
        outSheet.add(new SimpleRecord()
                .listText(toList("abc", "def"))
                .listInteger(toList(123, 456))
                .arrayText(toArray("abc", "def"))
                .arrayInteger(toArray(123, 456))
                .setText(toSet("abc", "def"))
                .setInteger(toSet(123, 456))
                .comment("項目が2つ"));
        
        outSheet.add(new SimpleRecord()
                .listText(toList("", null, ""))
                .listInteger(toList(0, null, null))
                .arrayText(toArray("", null, ""))
                .arrayInteger(toArray(0, null, null))
                .setText(toSet("", null, ""))
                .setInteger(toSet(0, null, null))
                .comment("空の項目のみ"));
        
        outSheet.add(new SimpleRecord()
                .listText(toList("abc", "", "def"))
                .listInteger(toList(123, null, 456))
                .arrayText(toArray("abc", "", "def"))
                .arrayInteger(toArray(123, null, 456))
                .setText(toSet("abc", "", "def"))
                .setInteger(toSet(123, null, 456))
                .comment("空の項目がある"));
        
        outSheet.add(new SimpleRecord()
                .listText(toList("  abc", " def "))
                .listInteger(toList(123, 456))
                .arrayText(toArray("  abc", " def "))
                .arrayInteger(toArray(123, 456))
                .setText(toSet("  abc", " def "))
                .setInteger(toSet(123, 456))
                .comment("空白がある"));
        
        // アノテーションありのデータ作成
        outSheet.add(new FormattedRecord()
                .comment("空文字"));
        
        outSheet.add(new FormattedRecord()
                .listText(toList("abc"))
                .listInteger(toList(123))
                .arrayText(toArray("abc"))
                .arrayInteger(toArray(123))
                .setText(toSet("abc"))
                .setInteger(toSet(123))
                .comment("項目が1つ"));
        
        outSheet.add(new FormattedRecord()
                .listText(toList("abc", "def"))
                .listInteger(toList(123, 456))
                .arrayText(toArray("abc", "def"))
                .arrayInteger(toArray(123, 456))
                .setText(toSet("abc", "def"))
                .setInteger(toSet(123, 456))
                .comment("項目が2つ"));
        
        outSheet.add(new FormattedRecord()
                .listText(toList("", null, ""))
                .listInteger(toList(0, null, null))
                .arrayText(toArray("", null, ""))
                .arrayInteger(toArray(0, null, null))
                .setText(toSet("", null, ""))
                .setInteger(toSet(0, null, null))
                .comment("空の項目のみ"));
        
        outSheet.add(new FormattedRecord()
                .listText(toList("abc", "", "def"))
                .listInteger(toList(123, null, 456))
                .arrayText(toArray("abc", "", "def"))
                .arrayInteger(toArray(123, null, 456))
                .setText(toSet("abc", "", "def"))
                .setInteger(toSet(123, null, 456))
                .comment("空の項目がある"));
        
        outSheet.add(new FormattedRecord()
                .listText(toList("  abc", " def "))
                .listInteger(toList(123, 456))
                .arrayText(toArray("  abc", " def "))
                .arrayInteger(toArray(123, 456))
                .setText(toSet("  abc", " def "))
                .setInteger(toSet(123, 456))
                .comment("空白がある"));
        
        
        // リスト型（任意の型）
        Date date1 = toUtilDate(toTimestamp("2016-03-15 00:00:00.000"));
        Date date2 = toUtilDate(toTimestamp("2016-03-16 00:00:00.000"));
        Date nullDate = null;
        
        outSheet.add(new CustomRecord()
                .comment("空文字"));
        
        outSheet.add(new CustomRecord()
                .listDate(toList(date1))
                .arrayDate(toArray(date1))
                .setDate(toSet(date1))
                .comment("項目が1つ"));
        
        outSheet.add(new CustomRecord()
                .listDate(toList(date1, date2))
                .arrayDate(toArray(date1, date2))
                .setDate(toSet(date1, date2))
                .comment("項目が２つ"));
        
        outSheet.add(new CustomRecord()
                .listDate(toList(nullDate, nullDate, nullDate))
                .arrayDate(toArray(nullDate, nullDate, nullDate))
                .setDate(toSet(nullDate, nullDate, nullDate))
                .comment("空の項目のみ"));
        
        outSheet.add(new CustomRecord()
                .listDate(toList(date1, nullDate, date2))
                .arrayDate(toArray(date1, nullDate, date2))
                .setDate(toSet(date1, nullDate, date2))
                .comment("空の項目がある"));
        
        outSheet.add(new CustomRecord()
                .listDate(toList(date1, date2))
                .arrayDate(toArray(date1, date2))
                .setDate(toSet(date1, date2))
                .comment("空白がある（※テスト不可）"));
        
        // ファイルへの書き込み
        XlsMapper mapper = new XlsMapper();
        mapper.getConig().setContinueTypeBindFailure(true);
        
        File outFile = new File("src/test/out/convert_collection.xlsx");
        try(InputStream template = new FileInputStream("src/test/data/convert_template.xlsx");
                OutputStream out = new FileOutputStream(outFile)) {
            
            mapper.save(template, out, outSheet);
        }
        
        // 書き込んだファイルを読み込み値の検証を行う。
        try(InputStream in = new FileInputStream(outFile)) {
            
            SheetBindingErrors errors = new SheetBindingErrors(CollectionSheet.class);
            
            CollectionSheet sheet = mapper.load(in, CollectionSheet.class, errors);
            
            if(sheet.simpleRecords != null) {
                assertThat(sheet.simpleRecords, hasSize(outSheet.simpleRecords.size()));
                
                for(int i=0; i < sheet.simpleRecords.size(); i++) {
                    assertRecord(sheet.simpleRecords.get(i), outSheet.simpleRecords.get(i), errors);
                }
            }
            
            if(sheet.formattedRecords != null) {
                assertThat(sheet.formattedRecords, hasSize(outSheet.formattedRecords.size()));
                
                for(int i=0; i < sheet.formattedRecords.size(); i++) {
                    assertRecord(sheet.formattedRecords.get(i), outSheet.formattedRecords.get(i), errors);
                }
            }
            
            if(sheet.customRecords != null) {
                assertThat(sheet.customRecords, hasSize(outSheet.customRecords.size()));
                
                for(int i=0; i < sheet.customRecords.size(); i++) {
                    assertRecord(sheet.customRecords.get(i), outSheet.customRecords.get(i), errors);
                }
            }
            
        }
    }
    
    /**
     * 書き込んだレコードを検証するための
     * @param inRecord
     * @param outRecord
     * @param errors
     */
    private void assertRecord(final SimpleRecord inRecord, final SimpleRecord outRecord, final SheetBindingErrors errors) {
        
        System.out.printf("%s - assertRecord::%s no=%d, comment=%s\n",
                this.getClass().getSimpleName(), inRecord.getClass().getSimpleName(), inRecord.no, inRecord.comment);
        
        if(inRecord.no == 1) {
            assertThat(inRecord.no, is(outRecord.no));
            assertThat(inRecord.listText, is(hasSize(0)));
            assertThat(inRecord.listInteger, is(hasSize(0)));
            assertThat(inRecord.arrayText, is(arrayWithSize(0)));
            assertThat(inRecord.arrayInteger, is(arrayWithSize(0)));
            assertThat(inRecord.setText, is(hasSize(0)));
            assertThat(inRecord.setInteger, is(hasSize(0)));
            assertThat(inRecord.comment, is(outRecord.comment));
            
        } else if(inRecord.no == 4) {
            assertThat(inRecord.no, is(outRecord.no));
            assertThat(inRecord.listText, is(hasSize(0)));
            assertThat(inRecord.listInteger, is(contains(0)));
            assertThat(inRecord.arrayText, is(arrayWithSize(0)));
            assertThat(inRecord.arrayInteger, is(arrayContaining(0)));
            assertThat(inRecord.setText, is(hasSize(0)));
            assertThat(inRecord.setInteger, is(containsInAnyOrder(0)));
            assertThat(inRecord.comment, is(outRecord.comment));
            
        } else if(inRecord.no == 5) {
            assertThat(inRecord.no, is(outRecord.no));
            assertThat(inRecord.listText, is(contains("abc", null, "def")));
            assertThat(inRecord.listInteger, is(contains(123, 456)));
            assertThat(inRecord.arrayText, is(arrayContaining("abc", null, "def")));
            assertThat(inRecord.arrayInteger, is(arrayContaining(123, 456)));
            assertThat(inRecord.setText, is(containsInAnyOrder("abc", null, "def")));
            assertThat(inRecord.setInteger, is(containsInAnyOrder(123, 456)));
            assertThat(inRecord.comment, is(outRecord.comment));
                    
        } else {
            assertThat(inRecord.no, is(outRecord.no));
            assertThat(inRecord.listText, is(outRecord.listText));
            assertThat(inRecord.listInteger, is(outRecord.listInteger));
            assertThat(inRecord.arrayText, is(outRecord.arrayText));
            assertThat(inRecord.arrayInteger, is(outRecord.arrayInteger));
            assertThat(inRecord.setText, is(outRecord.setText));
            assertThat(inRecord.setInteger, is(outRecord.setInteger));
            assertThat(inRecord.comment, is(outRecord.comment));
        }
        
    }
    
    /**
     * 書き込んだレコードを検証するための
     * @param inRecord
     * @param outRecord
     * @param errors
     */
    private void assertRecord(final FormattedRecord inRecord, final FormattedRecord outRecord, final SheetBindingErrors errors) {
        
        System.out.printf("%s - assertRecord::%s no=%d, comment=%s\n",
                this.getClass().getSimpleName(), inRecord.getClass().getSimpleName(), inRecord.no, inRecord.comment);

        if(inRecord.no == 1) {
            assertThat(inRecord.no, is(outRecord.no));
            assertThat(inRecord.listText, is(hasSize(0)));
            assertThat(inRecord.listInteger, is(contains(0)));
            assertThat(inRecord.arrayText, is(arrayWithSize(0)));
            assertThat(inRecord.arrayInteger, is(arrayContaining(0)));
            assertThat(inRecord.setText, is(hasSize(0)));
            assertThat(inRecord.setInteger, is(containsInAnyOrder(0)));
            assertThat(inRecord.comment, is(outRecord.comment));
            
        } else if(inRecord.no == 4) {
            assertThat(inRecord.no, is(outRecord.no));
            assertThat(inRecord.listText, is(hasSize(0)));
            assertThat(inRecord.listInteger, is(contains(0)));
            assertThat(inRecord.arrayText, is(arrayWithSize(0)));
            assertThat(inRecord.arrayInteger, is(arrayContaining(0)));
            assertThat(inRecord.setText, is(hasSize(0)));
            assertThat(inRecord.setInteger, is(containsInAnyOrder(0)));
            assertThat(inRecord.comment, is(outRecord.comment));
            
        } else if(inRecord.no == 5) {
            assertThat(inRecord.no, is(outRecord.no));
            assertThat(inRecord.listText, is(contains("abc", "def")));
            assertThat(inRecord.listInteger, is(contains(123, 456)));
            assertThat(inRecord.arrayText, is(arrayContaining("abc", "def")));
            assertThat(inRecord.arrayInteger, is(arrayContaining(123, 456)));
            assertThat(inRecord.setText, is(containsInAnyOrder("abc", "def")));
            assertThat(inRecord.setInteger, is(containsInAnyOrder(123, 456)));
            assertThat(inRecord.comment, is(outRecord.comment));
            
        } else if(inRecord.no == 6) {
            assertThat(inRecord.no, is(outRecord.no));
            assertThat(inRecord.listText, is(contains("abc", "def")));
            assertThat(inRecord.listInteger, is(contains(123, 456)));
            assertThat(inRecord.arrayText, is(arrayContaining("abc", "def")));
            assertThat(inRecord.arrayInteger, is(arrayContaining(123, 456)));
            assertThat(inRecord.setText, is(containsInAnyOrder("abc", "def")));
            assertThat(inRecord.setInteger, is(containsInAnyOrder(123, 456)));
            assertThat(inRecord.comment, is(outRecord.comment));
            
        } else {
            assertThat(inRecord.no, is(outRecord.no));
            assertThat(inRecord.listText, is(outRecord.listText));
            assertThat(inRecord.listInteger, is(outRecord.listInteger));
            assertThat(inRecord.arrayText, is(outRecord.arrayText));
            assertThat(inRecord.arrayInteger, is(outRecord.arrayInteger));
            assertThat(inRecord.setText, is(outRecord.setText));
            assertThat(inRecord.setInteger, is(outRecord.setInteger));
            assertThat(inRecord.comment, is(outRecord.comment));
        }
        
    }
    
    /**
     * 書き込んだレコードを検証するための
     * @param inRecord
     * @param outRecord
     * @param errors
     */
    private void assertRecord(final CustomRecord inRecord, final CustomRecord outRecord, final SheetBindingErrors errors) {
        
        System.out.printf("%s - assertRecord::%s no=%d, comment=%s\n",
                this.getClass().getSimpleName(), inRecord.getClass().getSimpleName(), inRecord.no, inRecord.comment);

        if(inRecord.no == 1) {
            assertThat(inRecord.no, is(outRecord.no));
            assertThat(inRecord.listDate, is(hasSize(0)));
            assertThat(inRecord.arrayDate, is(arrayWithSize(0)));
            assertThat(inRecord.setDate, is(hasSize(0)));
            assertThat(inRecord.comment, is(outRecord.comment));
            
        } else if(inRecord.no == 4) {
            assertThat(inRecord.no, is(outRecord.no));
            assertThat(inRecord.listDate, is(hasSize(0)));
            assertThat(inRecord.arrayDate, is(arrayWithSize(0)));
            assertThat(inRecord.setDate, is(hasSize(0)));
            assertThat(inRecord.comment, is(outRecord.comment));
            
        } else if(inRecord.no == 5) {
            
            Date date1 = toUtilDate(toTimestamp("2016-03-15 00:00:00.000"));
            Date date2 = toUtilDate(toTimestamp("2016-03-16 00:00:00.000"));
            
            assertThat(inRecord.no, is(outRecord.no));
            assertThat(inRecord.listDate, contains(date1, date2));
            assertThat(inRecord.arrayDate, arrayContaining(date1, date2));
            assertThat(inRecord.setDate, contains(date1, date2));
            assertThat(inRecord.comment, is(outRecord.comment));
            
        } else if(inRecord.no == 6) {
            Date date1 = toUtilDate(toTimestamp("2016-03-15 00:00:00.000"));
            Date date2 = toUtilDate(toTimestamp("2016-03-16 00:00:00.000"));
            
            assertThat(inRecord.no, is(outRecord.no));
            assertThat(inRecord.listDate, contains(date1, date2));
            assertThat(inRecord.arrayDate, arrayContaining(date1, date2));
            assertThat(inRecord.setDate, contains(date1, date2));
            assertThat(inRecord.comment, is(outRecord.comment));
            
        } else {
            assertThat(inRecord.no, is(outRecord.no));
            assertThat(inRecord.listDate, is(outRecord.listDate));
            assertThat(inRecord.arrayDate, is(outRecord.arrayDate));
            assertThat(inRecord.setDate, is(outRecord.setDate));
            assertThat(inRecord.comment, is(outRecord.comment));
        }
        
    }
    
    @XlsSheet(name="リスト型")
    private static class CollectionSheet {
        
        @XlsHint(order=1)
        @XlsHorizontalRecords(tableLabel="リスト型（アノテーションなし）", terminal=RecordTerminal.Border, ignoreEmptyRecord=true,
                overRecord=OverRecordOperate.Insert)
        private List<SimpleRecord> simpleRecords;
        
        @XlsHint(order=2)
        @XlsHorizontalRecords(tableLabel="リスト型（初期値、書式）", terminal=RecordTerminal.Border, ignoreEmptyRecord=true,
                overRecord=OverRecordOperate.Insert)
        private List<FormattedRecord> formattedRecords;
        
        @XlsHint(order=3)
        @XlsHorizontalRecords(tableLabel="リスト型（任意の型）", terminal=RecordTerminal.Border, ignoreEmptyRecord=true,
                overRecord=OverRecordOperate.Insert)
        private List<CustomRecord> customRecords;
        
        /**
         * レコードを追加する。noを自動的に付与する。
         * @param record
         * @return
         */
        public CollectionSheet add(SimpleRecord record) {
            if(simpleRecords == null) {
                this.simpleRecords = new ArrayList<>();
            }
            this.simpleRecords.add(record);
            record.no(simpleRecords.size());
            return this;
        }
        
        /**
         * レコードを追加する。noを自動的に付与する。
         * @param record
         * @return
         */
        public CollectionSheet add(FormattedRecord record) {
            if(formattedRecords == null) {
                this.formattedRecords = new ArrayList<>();
            }
            this.formattedRecords.add(record);
            record.no(formattedRecords.size());
            return this;
        }
        
        /**
         * レコードを追加する。noを自動的に付与する。
         * @param record
         * @return
         */
        public CollectionSheet add(CustomRecord record) {
            if(customRecords == null) {
                this.customRecords = new ArrayList<>();
            }
            this.customRecords.add(record);
            record.no(customRecords.size());
            return this;
        }
        
    }
    
    /**
     * リスト型 - アノテーションなし。
     *
     */
    private static class SimpleRecord {
        
        private Map<String, Point> positions;
        
        private Map<String, String> labels;
        
        @XlsColumn(columnName="No.")
        private int no;
        
        @XlsColumn(columnName="List（文字列）")
        private List<String> listText;
        
        @XlsColumn(columnName="List（数値）")
        private List<Integer> listInteger;
        
        @XlsColumn(columnName="Array（文字列）")
        private String[] arrayText;
        
        @XlsColumn(columnName="Array（数値）")
        private Integer[] arrayInteger;
        
        @XlsColumn(columnName="Set（文字列）")
        private Set<String> setText;
        
        @XlsColumn(columnName="Set（数値）")
        private Set<Integer> setInteger;
        
        @XlsColumn(columnName="備考")
        private String comment;
        
        @XlsIsEmpty
        public boolean isEmpty() {
            return IsEmptyBuilder.reflectionIsEmpty(this, "positions", "labels", "no");
        }
        
        public SimpleRecord no(int no) {
            this.no = no;
            return this;
        }
        
        public SimpleRecord listText(List<String> listText) {
            this.listText = listText;
            return this;
        }
        
        public SimpleRecord listInteger(List<Integer> listInteger) {
            this.listInteger = listInteger;
            return this;
        }
        
        public SimpleRecord arrayText(String[] arrayText) {
            this.arrayText = arrayText;
            return this;
        }
        
        public SimpleRecord arrayInteger(Integer[] arrayInteger) {
            this.arrayInteger = arrayInteger;
            return this;
        }
        
        public SimpleRecord setText(Set<String> setText) {
            this.setText = setText;
            return this;
        }
        
        public SimpleRecord setInteger(Set<Integer> setInteger) {
            this.setInteger = setInteger;
            return this;
        }
        
        public SimpleRecord comment(String comment) {
            this.comment = comment;
            return this;
        }
        
    }
    
    private static class FormattedRecord {
        
        private Map<String, Point> positions;
        
        private Map<String, String> labels;
        
        @XlsColumn(columnName="No.")
        private int no;
        
        @XlsConverter(trim=true)
        @XlsArrayConverter(separator="\n", ignoreEmptyItem=true)
        @XlsColumn(columnName="List（文字列）")
        private List<String> listText;
        
        @XlsConverter(trim=true, defaultValue="0")
        @XlsArrayConverter(separator=";", ignoreEmptyItem=true)
        @XlsColumn(columnName="List（数値）")
        private List<Integer> listInteger;
        
        @XlsConverter(trim=true)
        @XlsArrayConverter(separator="\n", ignoreEmptyItem=true)
        @XlsColumn(columnName="Array（文字列）")
        private String[] arrayText;
        
        @XlsConverter(trim=true, defaultValue="0")
        @XlsArrayConverter(separator=";", ignoreEmptyItem=true)
        @XlsColumn(columnName="Array（数値）")
        private Integer[] arrayInteger;
        
        @XlsConverter(trim=true)
        @XlsArrayConverter(separator="\n", ignoreEmptyItem=true)
        @XlsColumn(columnName="Set（文字列）")
        private Set<String> setText;
        
        @XlsConverter(trim=true, defaultValue="0")
        @XlsArrayConverter(separator=";", ignoreEmptyItem=true)
        @XlsColumn(columnName="Set（数値）")
        private Set<Integer> setInteger;
        
        @XlsColumn(columnName="備考")
        private String comment;
        
        @XlsIsEmpty
        public boolean isEmpty() {
            return IsEmptyBuilder.reflectionIsEmpty(this, "positions", "labels", "no");
        }
        
        public FormattedRecord no(int no) {
            this.no = no;
            return this;
        }
        
        public FormattedRecord listText(List<String> listText) {
            this.listText = listText;
            return this;
        }
        
        public FormattedRecord listInteger(List<Integer> listInteger) {
            this.listInteger = listInteger;
            return this;
        }
        
        public FormattedRecord arrayText(String[] arrayText) {
            this.arrayText = arrayText;
            return this;
        }
        
        public FormattedRecord arrayInteger(Integer[] arrayInteger) {
            this.arrayInteger = arrayInteger;
            return this;
        }
        
        public FormattedRecord setText(Set<String> setText) {
            this.setText = setText;
            return this;
        }
        
        public FormattedRecord setInteger(Set<Integer> setInteger) {
            this.setInteger = setInteger;
            return this;
        }
        
        public FormattedRecord comment(String comment) {
            this.comment = comment;
            return this;
        }
        
    }
    
    private static class CustomRecord {
        
        private Map<String, Point> positions;
        
        private Map<String, String> labels;
        
        @XlsColumn(columnName="No.")
        private int no;
        
        @XlsConverter(trim=true)
        @XlsArrayConverter(separator="\n", ignoreEmptyItem=true, itemConverterClass=DateItemConverter.class)
        @XlsColumn(columnName="List（Date型）")
        private List<Date> listDate;
        
        @XlsConverter(trim=true)
        @XlsArrayConverter(separator=";", ignoreEmptyItem=true, itemConverterClass=DateItemConverter.class)
        @XlsColumn(columnName="Array（Date型）")
        private Date[] arrayDate;
        
        @XlsConverter(trim=true)
        @XlsArrayConverter(separator=",", ignoreEmptyItem=true, itemConverterClass=DateItemConverter.class)
        @XlsColumn(columnName="Set（Date型）")
        private Set<Date> setDate;
        
        @XlsColumn(columnName="備考")
        private String comment;
        
        @XlsIsEmpty
        public boolean isEmpty() {
            return IsEmptyBuilder.reflectionIsEmpty(this, "positions", "labels", "no");
        }
        
        public CustomRecord no(int no) {
            this.no = no;
            return this;
        }
        
        public CustomRecord listDate(List<Date> listDate) {
            this.listDate = listDate;
            return this;
        }
        
        public CustomRecord arrayDate(Date[] arrayDate) {
            this.arrayDate = arrayDate;
            return this;
        }
        
        public CustomRecord setDate(Set<Date> setDate) {
            this.setDate = setDate;
            return this;
        }
        
        public CustomRecord comment(String comment) {
            this.comment = comment;
            return this;
        }
        
        private static class DateItemConverter implements ItemConverter<Date> {
            
            @Override
            public Date convertToObject(final String text, final Class<Date> targetClass) throws ConversionException {
                SimpleDateFormat formatter = new SimpleDateFormat("yyyy年MM月dd日");
                try {
                    return formatter.parse(text);
                } catch (ParseException e) {
                    String message = String.format("fail parse '%s' to java.util.Date", text);
                    throw new ConversionException(message, e, targetClass);
                }
            }
            
            @Override
            public String convertToString(final Date value) {
                
                if(value == null) {
                    return "";
                }
                
                SimpleDateFormat formatter = new SimpleDateFormat("yyyy年MM月dd日");
                return formatter.format(value);
            }
            
        }
        
    }
}
