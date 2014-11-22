package com.gh.mygreen.xlsmapper.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 垂直方向に連続する列をListまたは配列にマッピングする際に指定します。
 * <p>表には最左部にテーブルの名称と列名を記述した行が必要になります。
 * An annotation for the property which is mapped to the vertical table records.
 * 
 * TODO Is this necessary?
 * 
 * @author Naoki Takezoe
 */
@Target({ElementType.METHOD, ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface XlsVerticalRecords {
    
    /**
     * レコードが見つからない場合に、エラーとしないで、無視して処理を続行させてい場合trueを指定します。
     * 
     * @return
     */
    boolean optional() default false;
    
    /**
     * 表の見出し（タイトル）ラベル。値を指定した場合、ラベルと一致するセルを起点に走査を行う。
     * <p>属性{@link #tableAddress()}のどちらか一方を指定可能。
     */
    String tableLabel() default "";
    
    /**
     * 表の見出し（タイトル）が存在するセルのアドレス。値を指定した場合、指定したアドレスを起点に走査を行う。
     * <p>属性{@link #tableLabel()()}のどちらか一方を指定可能。
     * <p>'A1'などのようにシートのアドレスで指定する。
     */
    String tableAddress() default "";
    
    /**
     * テーブルが他のテーブルと連続しておりterminal属性でBorder、Emptyのいずれを指定しても終端を検出できない場合があります。 
     * このような場合はterminateLabel属性で終端を示すセルの文字列を指定します。
     * @return
     */
    String terminateLabel() default "";
    
    /**
     * 表の開始位置（見出し列）セルの行番号を指定する。
     * <p>値は'0'から始まる。
     * @return
     */
    int headerColumn() default -1;
    
    /**
     * 表の開始位置（見出し行）セルの行番号を指定する。
     * <p>値は'0'から始まる。
     * @return
     */
    int headerRow() default -1;
    
    /**
     * 表の開始位置のセルのアドレスを'A1'などのように指定します。値を指定した場合、指定したアドレスを起点に走査を行います
     * <p>属性{@link #headerRow()},{@link #headerColumn()}のどちらか一方を指定可能です
     */
    String headerAddress() default "";
    
    /** 
     * レコードのマッピング先のクラスを指定します。
     * <p>指定しない場合は、Genericsの定義タイプを使用します。
     */
    Class<?> recordClass() default Object.class;
    
    /** 
     * 表の終端の種類を指定します
     */
    RecordTerminal terminal() default RecordTerminal.Empty;
    
    /**
     * 下方向に向かって指定したセル数分を検索し、最初に発見した空白以外のセルを見出しとします。
     * @return
     */
    int range() default 1;
    
    /**
     * テーブルのカラムが指定数見つかったタイミングで Excelシートの走査を終了したい場合に指定します。
     * <p>主に無駄な走査を抑制したい場合にしますが、 {@link XlsIterateTables}使用時に、
     * テーブルが隣接しており終端を検出できない場合などに カラム数を明示的に指定してテーブルの区切りを指定する場合にも使用できます。 
     * @return
     */
    int headerLimit() default 0;
    
    /**
     * 書き込み時にデータのレコード数に対してシートのレコードが足りない場合の操作を指定します。
     * {@link XlsVerticalRecords}の場合、{@link OverRecordOperate#Insert}は対応していません。
     * @return
     */
    OverRecordOperate overRecord() default OverRecordOperate.Break;
    
    /**
     * 書き込み時にデータのレコード数に対してシートのレコードが余っている際の操作を指定します。
     * {@link XlsVerticalRecords}の場合、{@link RemainedRecordOperate#Delete}は対応していません。
     * @return
     */
    RemainedRecordOperate remainedRecord() default RemainedRecordOperate.None; 
    
    /**
     * 空のレコードの場合、処理をスキップするかどうか。
     * <p>レコードの判定用のメソッドに、アノテーション{@link XlsIsEmpty}を付与する必要があります。
     * @return
     */
    boolean skipEmptyRecord() default false;
}
