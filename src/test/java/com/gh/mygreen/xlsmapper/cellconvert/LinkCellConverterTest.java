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
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.Test;

import com.gh.mygreen.xlsmapper.IsEmptyBuilder;
import com.gh.mygreen.xlsmapper.XlsMapper;
import com.gh.mygreen.xlsmapper.annotation.OverRecordOperate;
import com.gh.mygreen.xlsmapper.annotation.RecordTerminal;
import com.gh.mygreen.xlsmapper.annotation.XlsColumn;
import com.gh.mygreen.xlsmapper.annotation.XlsConverter;
import com.gh.mygreen.xlsmapper.annotation.XlsHint;
import com.gh.mygreen.xlsmapper.annotation.XlsHorizontalRecords;
import com.gh.mygreen.xlsmapper.annotation.XlsIsEmpty;
import com.gh.mygreen.xlsmapper.annotation.XlsSheet;
import com.gh.mygreen.xlsmapper.cellconvert.converter.URICellConverter;
import com.gh.mygreen.xlsmapper.validation.SheetBindingErrors;

/**
 * リンクの変換テスト。
 * <p>下記のConverterのテスタ
 * <ol>
 *   <li>{@link URICellConverter}
 *   <li>{@link CellLink}
 * 
 * @since 0.5
 * @author T.TSUCHIE
 *
 */
public class LinkCellConverterTest {
    
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
     * リンクの読み込みテスト
     */
    @Test
    public void test_load_link() throws Exception {
        XlsMapper mapper = new XlsMapper();
        mapper.getConig().setContinueTypeBindFailure(true);
        
        try(InputStream in = new FileInputStream("src/test/data/convert.xlsx")) {
            SheetBindingErrors errors = new SheetBindingErrors(LinkSheet.class);
            
            LinkSheet sheet = mapper.load(in, LinkSheet.class, errors);
            
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
            
        }
    }
    
    private void assertRecord(final SimpleRecord record, final SheetBindingErrors errors) throws URISyntaxException {
        if(record.no == 1) {
            // 空文字
            assertThat(record.uri, is(nullValue()));
            assertThat(record.link, is(nullValue()));
            
        } else if(record.no == 2) {
            // URL（ラベルが同じ）
            assertThat(record.uri, is(new URI("http://www.google.co.jp/")));
            assertThat(record.link, is(new CellLink("http://www.google.co.jp/", "http://www.google.co.jp/")));
            
        } else if(record.no == 3) {
            // URL（ラベルが異なる）
            assertThat(record.uri, is(new URI("http://www.google.co.jp/")));
            assertThat(record.link, is(new CellLink("http://www.google.co.jp/", "Googleサイト")));
            
        } else if(record.no == 4) {
            // 文字列
            assertThat(record.uri, is(new URI("http://www.google.co.jp/")));
            assertThat(record.link, is(new CellLink(null, "http://www.google.co.jp/")));
            
        } else if(record.no == 5) {
            // メールアドレス
            assertThat(record.uri, is(new URI("mailto:hoge@google.com")));
            assertThat(record.link, is(new CellLink("mailto:hoge@google.com", "hoge@google.com")));
            
        } else if(record.no == 6) {
            // ファイルパス
            assertThat(record.uri, is(new URI("convert.xlsx")));
            assertThat(record.link, is(new CellLink("convert.xlsx", ".\\convert.xlsx")));
            
        } else if(record.no == 7) {
            // セルへのリンク
            assertThat(record.uri, is(new URI("リンク型!A1")));
            assertThat(record.link, is(new CellLink("リンク型!A1", "セルへのリンク")));
            
        } else if(record.no == 8) {
            // 不正なリンク＋ラベルに空白を含む
            assertThat(record.uri, is(new URI("http://invalid.uri")));
            assertThat(record.link, is(new CellLink("http://invalid.uri", "  空白を含むリンク  ")));
            
        } else if(record.no == 9) {
            // 空白の文字列
            assertThat(cellFieldError(errors, cellAddress(record.positions.get("uri"))).isTypeBindFailure(), is(true));
            assertThat(record.link, is(new CellLink(null, "  http://www.google.co.jp/  ")));
            
        } else if(record.no == 10) {
            // URL（ラベルが空白）
            assertThat(record.uri, is(new URI("http://www.google.co.jp/")));
            assertThat(record.link, is(new CellLink("http://www.google.co.jp/", "  ")));
            
        } else if(record.no == 11) {
            // 空白の文字
            assertThat(cellFieldError(errors, cellAddress(record.positions.get("uri"))).isTypeBindFailure(), is(true));
            assertThat(record.link, is(new CellLink(null, "   ")));
            
        } else {
            fail(String.format("not support test case. No=%d.", record.no));
        }
        
    }
    
    private void assertRecord(final FormattedRecord record, final SheetBindingErrors errors) throws URISyntaxException {
        if(record.no == 1) {
            // 空文字
            assertThat(record.uri, is(new URI("http://myhome.com/")));
            assertThat(record.link, is(new CellLink("http://myhome.com/", "http://myhome.com/")));
            
        } else if(record.no == 2) {
            // URL（ラベルが同じ）
            assertThat(record.uri, is(new URI("http://www.google.co.jp/")));
            assertThat(record.link, is(new CellLink("http://www.google.co.jp/", "http://www.google.co.jp/")));
            
        } else if(record.no == 3) {
            // URL（ラベルが異なる）
            assertThat(record.uri, is(new URI("http://www.google.co.jp/")));
            assertThat(record.link, is(new CellLink("http://www.google.co.jp/", "Googleサイト")));
            
        } else if(record.no == 4) {
            // 文字列
            assertThat(record.uri, is(new URI("http://www.google.co.jp/")));
            assertThat(record.link, is(new CellLink(null, "http://www.google.co.jp/")));
            
        } else if(record.no == 5) {
            // メールアドレス
            assertThat(record.uri, is(new URI("mailto:hoge@google.com")));
            assertThat(record.link, is(new CellLink("mailto:hoge@google.com", "hoge@google.com")));
            
        } else if(record.no == 6) {
            // ファイルパス
            assertThat(record.uri, is(new URI("convert.xlsx")));
            assertThat(record.link, is(new CellLink("convert.xlsx", ".\\convert.xlsx")));
            
        } else if(record.no == 7) {
            // セルへのリンク
            assertThat(record.uri, is(new URI("リンク型!A1")));
            assertThat(record.link, is(new CellLink("リンク型!A1", "セルへのリンク")));
            
        } else if(record.no == 8) {
            // 不正なリンク＋ラベルに空白を含む
            assertThat(record.uri, is(new URI("http://invalid.uri")));
            assertThat(record.link, is(new CellLink("http://invalid.uri", "空白を含むリンク")));
            
        } else if(record.no == 9) {
            // 空白の文字列
            assertThat(record.uri, is(new URI("http://www.google.co.jp/")));
            assertThat(record.link, is(new CellLink(null, "http://www.google.co.jp/")));
            
        } else if(record.no == 10) {
            // URL（ラベルが空白）
            assertThat(record.uri, is(new URI("http://www.google.co.jp/")));
            assertThat(record.link, is(new CellLink("http://www.google.co.jp/", "")));
            
        } else if(record.no == 11) {
            // 空白の文字
            assertThat(record.uri, is(nullValue()));
            assertThat(record.link, is(nullValue()));
            
        } else {
            fail(String.format("not support test case. No=%d.", record.no));
        }
    }
    
    /**
     * リンク型の書き込みテスト
     */
    @Test
    public void test_save_link() throws Exception {
        
        // テストデータの作成
        final LinkSheet outSheet = new LinkSheet();
        
        // アノテーションなしのデータ作成
        outSheet.add(new SimpleRecord()
                .comment("空文字"));
        
        outSheet.add(new SimpleRecord()
                .uri(new URI("http://www.google.co.jp/"))
                .link(new CellLink("http://www.google.co.jp/", "http://www.google.co.jp/"))
                .comment("URL（ラベルが同じ）"));
        
        outSheet.add(new SimpleRecord()
                .uri(new URI("http://www.google.co.jp/"))
                .link(new CellLink("http://www.google.co.jp/", "Googleサイト"))
                .comment("URL（ラベルが異なる）"));
        
        outSheet.add(new SimpleRecord()
                .uri(new URI("hoge@google.com"))
                .link(new CellLink("hoge@google.com", "hoge@google.com"))
                .comment("URL（メールアドレス）"));
        
        outSheet.add(new SimpleRecord()
                .uri(new URI("convert.xlsx"))
                .link(new CellLink("convert.xlsx", ".\\convert.xlsx"))
                .comment("ファイルパス"));
        
        outSheet.add(new SimpleRecord()
                .uri(new URI("A1"))
                .link(new CellLink("A1", "セルへのリンク"))
                .comment("セルへのリンク"));
        
        outSheet.add(new SimpleRecord()
                .uri(new URI("http://www.google.co.jp/"))
                .link(new CellLink("http://www.google.co.jp/", " 空白を含むリンク "))
                .comment("空白を含むリンク"));
        
        outSheet.add(new SimpleRecord()
            .uri(new URI("http://www.google.co.jp/"))
            .link(new CellLink("http://www.google.co.jp/", "  "))
            .comment("ラベルが空白"));
        
        // アノテーションありのデータ作成
        outSheet.add(new FormattedRecord()
                .comment("空文字"));
        
        outSheet.add(new FormattedRecord()
        .uri(new URI("http://www.google.co.jp/"))
        .link(new CellLink("http://www.google.co.jp/", "http://www.google.co.jp/"))
        .comment("URL（ラベルが同じ）"));
        
        outSheet.add(new FormattedRecord()
                .uri(new URI("http://www.google.co.jp/"))
                .link(new CellLink("http://www.google.co.jp/", "Googleサイト"))
                .comment("URL（ラベルが異なる）"));
        
        outSheet.add(new FormattedRecord()
                .uri(new URI("hoge@google.com"))
                .link(new CellLink("hoge@google.com", "hoge@google.com"))
                .comment("URL（メールアドレス）"));
        
        outSheet.add(new FormattedRecord()
                .uri(new URI("convert.xlsx"))
                .link(new CellLink("convert.xlsx", ".\\convert.xlsx"))
                .comment("ファイルパス"));
        
        outSheet.add(new FormattedRecord()
                .uri(new URI("A1"))
                .link(new CellLink("A1", "セルへのリンク"))
                .comment("セルへのリンク"));
        
        outSheet.add(new FormattedRecord()
                .uri(new URI("http://www.google.co.jp/"))
                .link(new CellLink("http://www.google.co.jp/", " 空白を含むリンク "))
                .comment("空白を含むリンク"));
        
        outSheet.add(new FormattedRecord()
            .uri(new URI("http://www.google.co.jp/"))
            .link(new CellLink("http://www.google.co.jp/", "  "))
            .comment("ラベルが空白"));
                
        // ファイルへの書き込み
        XlsMapper mapper = new XlsMapper();
        mapper.getConig().setContinueTypeBindFailure(true);
        
        File outFile = new File("src/test/out/convert_link.xlsx");
        try(InputStream template = new FileInputStream("src/test/data/convert_template.xlsx");
                OutputStream out = new FileOutputStream(outFile)) {
            
            mapper.save(template, out, outSheet);
        }
        
        // 書き込んだファイルを読み込み値の検証を行う。
        try(InputStream in = new FileInputStream(outFile)) {
            
            SheetBindingErrors errors = new SheetBindingErrors(LinkSheet.class);
            
            LinkSheet sheet = mapper.load(in, LinkSheet.class, errors);
            
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
        
        assertThat(inRecord.no, is(outRecord.no));
        assertThat(inRecord.uri, is(outRecord.uri));
        assertThat(inRecord.link, is(outRecord.link));
        assertThat(inRecord.comment, is(outRecord.comment));
    }
    
    /**
     * 書き込んだレコードを検証するための
     * @param inRecord
     * @param outRecord
     * @param errors
     * @throws URISyntaxException 
     */
    private void assertRecord(final FormattedRecord inRecord, final FormattedRecord outRecord, final SheetBindingErrors errors) throws URISyntaxException {
        
        System.out.printf("%s - assertRecord::%s no=%d, comment=%s\n",
                this.getClass().getSimpleName(), inRecord.getClass().getSimpleName(), inRecord.no, inRecord.comment);
        
        if(inRecord.no == 1) {
            assertThat(inRecord.no, is(outRecord.no));
            assertThat(inRecord.uri, is(new URI("http://myhome.com/")));
            assertThat(inRecord.link, is(new CellLink("http://myhome.com/", "http://myhome.com/")));
            assertThat(inRecord.comment, is(outRecord.comment));
            
        } else if(inRecord.no == 7) {
            assertThat(inRecord.no, is(outRecord.no));
            assertThat(inRecord.uri, is(new URI("http://www.google.co.jp/")));
            assertThat(inRecord.link, is(new CellLink("http://www.google.co.jp/", "空白を含むリンク")));
            assertThat(inRecord.comment, is(outRecord.comment));
            
        } else if(inRecord.no == 8) {
            assertThat(inRecord.no, is(outRecord.no));
            assertThat(inRecord.uri, is(new URI("http://www.google.co.jp/")));
            assertThat(inRecord.link, is(new CellLink("http://www.google.co.jp/", "")));
            assertThat(inRecord.comment, is(outRecord.comment));
            
        } else {
            assertThat(inRecord.no, is(outRecord.no));
            assertThat(inRecord.uri, is(outRecord.uri));
            assertThat(inRecord.link, is(outRecord.link));
            assertThat(inRecord.comment, is(outRecord.comment));
        }
    }
    
    @XlsSheet(name="リンク型")
    private static class LinkSheet {
        
        @XlsHint(order=1)
        @XlsHorizontalRecords(tableLabel="リンク型（アノテーションなし）", terminal=RecordTerminal.Border, ignoreEmptyRecord=true,
                overRecord=OverRecordOperate.Insert)
        private List<SimpleRecord> simpleRecords;
        
        @XlsHint(order=2)
        @XlsHorizontalRecords(tableLabel="リンク型（初期値、書式）", terminal=RecordTerminal.Border, ignoreEmptyRecord=true,
                overRecord=OverRecordOperate.Insert)
        private List<FormattedRecord> formattedRecords;
        
        /**
         * レコードを追加する。noを自動的に付与する。
         * @param record
         * @return
         */
        public LinkSheet add(SimpleRecord record) {
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
        public LinkSheet add(FormattedRecord record) {
            if(formattedRecords == null) {
                this.formattedRecords = new ArrayList<>();
            }
            this.formattedRecords.add(record);
            record.no(formattedRecords.size());
            return this;
        }
    }
    
    /**
     * リンク型 - アノテーションなし
     *
     */
    private static class SimpleRecord {
        
        private Map<String, Point> positions;
        
        private Map<String, String> labels;
        
        @XlsColumn(columnName="No.")
        private int no;
        
        @XlsColumn(columnName="URI")
        private URI uri;
        
        @XlsColumn(columnName="CellLink")
        private CellLink link;
        
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
        
        public SimpleRecord uri(URI uri) {
            this.uri = uri;
            return this;
        }
        
        public SimpleRecord link(CellLink link) {
            this.link = link;
            return this;
        }
        
        public SimpleRecord comment(String comment) {
            this.comment = comment;
            return this;
        }
        
    }
    
    /**
     * リンク型 - 初期値など
     *
     */
    private static class FormattedRecord {
        
        private Map<String, Point> positions;
        
        private Map<String, String> labels;
        
        @XlsColumn(columnName="No.")
        private int no;
        
        @XlsConverter(trim=true, defaultValue="http://myhome.com/")
        @XlsColumn(columnName="URI")
        private URI uri;
        
        @XlsConverter(trim=true, defaultValue="http://myhome.com/")
        @XlsColumn(columnName="CellLink")
        private CellLink link;
        
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
        
        public FormattedRecord uri(URI uri) {
            this.uri = uri;
            return this;
        }
        
        public FormattedRecord link(CellLink link) {
            this.link = link;
            return this;
        }
        
        public FormattedRecord comment(String comment) {
            this.comment = comment;
            return this;
        }
        
    }
    
}
