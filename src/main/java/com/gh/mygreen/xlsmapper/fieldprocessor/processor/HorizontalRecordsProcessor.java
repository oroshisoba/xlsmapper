package com.gh.mygreen.xlsmapper.fieldprocessor.processor;

import java.awt.Point;
import java.lang.reflect.Array;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.DataValidation;
import org.apache.poi.ss.usermodel.Name;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.util.AreaReference;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.ss.util.CellRangeAddressList;
import org.apache.poi.ss.util.CellReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.gh.mygreen.xlsmapper.AnnotationInvalidException;
import com.gh.mygreen.xlsmapper.CellCommentStore;
import com.gh.mygreen.xlsmapper.LoadingWorkObject;
import com.gh.mygreen.xlsmapper.NeedProcess;
import com.gh.mygreen.xlsmapper.POIUtils;
import com.gh.mygreen.xlsmapper.SavingWorkObject;
import com.gh.mygreen.xlsmapper.Utils;
import com.gh.mygreen.xlsmapper.XlsMapperConfig;
import com.gh.mygreen.xlsmapper.XlsMapperException;
import com.gh.mygreen.xlsmapper.annotation.OverRecordOperate;
import com.gh.mygreen.xlsmapper.annotation.RecordTerminal;
import com.gh.mygreen.xlsmapper.annotation.RemainedRecordOperate;
import com.gh.mygreen.xlsmapper.annotation.XlsColumn;
import com.gh.mygreen.xlsmapper.annotation.XlsHorizontalRecords;
import com.gh.mygreen.xlsmapper.annotation.XlsIsEmpty;
import com.gh.mygreen.xlsmapper.annotation.XlsListener;
import com.gh.mygreen.xlsmapper.annotation.XlsMapColumns;
import com.gh.mygreen.xlsmapper.annotation.XlsNestedRecords;
import com.gh.mygreen.xlsmapper.annotation.XlsPostLoad;
import com.gh.mygreen.xlsmapper.annotation.XlsPostSave;
import com.gh.mygreen.xlsmapper.annotation.XlsPreLoad;
import com.gh.mygreen.xlsmapper.annotation.XlsPreSave;
import com.gh.mygreen.xlsmapper.cellconvert.CellConverter;
import com.gh.mygreen.xlsmapper.cellconvert.TypeBindException;
import com.gh.mygreen.xlsmapper.fieldprocessor.AbstractFieldProcessor;
import com.gh.mygreen.xlsmapper.fieldprocessor.CellAddress;
import com.gh.mygreen.xlsmapper.fieldprocessor.CellNotFoundException;
import com.gh.mygreen.xlsmapper.fieldprocessor.FieldAdaptor;
import com.gh.mygreen.xlsmapper.fieldprocessor.MergedRecord;
import com.gh.mygreen.xlsmapper.fieldprocessor.NestMergedSizeException;
import com.gh.mygreen.xlsmapper.fieldprocessor.RecordHeader;
import com.gh.mygreen.xlsmapper.fieldprocessor.RecordsProcessorUtil;
import com.gh.mygreen.xlsmapper.xml.AnnotationReadException;
import com.gh.mygreen.xlsmapper.xml.AnnotationReader;


/**
 * アノテーション{@link XlsHorizontalRecords}を処理するクラス。
 * 
 * @version 1.4
 * @author Naoki Takezoe
 * @author T.TSUCHIE
 *
 */
public class HorizontalRecordsProcessor extends AbstractFieldProcessor<XlsHorizontalRecords> {
    
    private static Logger logger = LoggerFactory.getLogger(HorizontalRecordsProcessor.class);
    
    @Override
    public void loadProcess(final Sheet sheet, final Object beansObj, final XlsHorizontalRecords anno, final FieldAdaptor adaptor,
            final XlsMapperConfig config, final LoadingWorkObject work) throws XlsMapperException {
        
        // ラベルの設定
        if(Utils.isNotEmpty(anno.tableLabel())) {
            try {
                final Cell tableLabelCell = Utils.getCell(sheet, anno.tableLabel(), 0, config);
                Utils.setLabel(POIUtils.getCellContents(tableLabelCell, config.getCellFormatter()), beansObj, adaptor.getName());
            } catch(CellNotFoundException e) {
                
            }
        }
        
        final Class<?> clazz = adaptor.getTargetClass();
        if(Collection.class.isAssignableFrom(clazz)) {
            
            Class<?> recordClass = anno.recordClass();
            if(recordClass == Object.class) {
                recordClass = adaptor.getLoadingGenericClassType();
            }
            
            List<?> value = loadRecords(sheet, anno, adaptor, recordClass, config, work);
            if(value != null) {
                @SuppressWarnings({"unchecked", "rawtypes"})
                Collection<?> collection = Utils.convertListToCollection(value, (Class<Collection>)clazz, config.getBeanFactory());
                adaptor.setValue(beansObj, collection);
            }
            
        } else if(clazz.isArray()) {
            
            Class<?> recordClass = anno.recordClass();
            if(recordClass == Object.class) {
                recordClass = adaptor.getLoadingGenericClassType();
            }
            
            final List<?> value = loadRecords(sheet, anno, adaptor, recordClass, config, work);
            if(value != null) {
                final Object array = Array.newInstance(recordClass, value.size());
                for(int i=0; i < value.size(); i++) {
                    Array.set(array, i, value.get(i));
                }
                
                adaptor.setValue(beansObj, array);
            }
            
        } else {
            throw new AnnotationInvalidException(
                    String.format("With '%s', '@XlsHorizontalRecords' should only granted Collection(List/Set) or Array. : %s", 
                            adaptor.getNameWithClass(), clazz.getName()),
                            anno);
        }
        
    }
    
    private List<?> loadRecords(final Sheet sheet, XlsHorizontalRecords anno, final FieldAdaptor adaptor, 
            final Class<?> recordClass, final XlsMapperConfig config, final LoadingWorkObject work) throws XlsMapperException {
        
        RecordsProcessorUtil.checkLoadingNestedRecordClass(recordClass, adaptor, work.getAnnoReader());
        
        // get table starting position
        final CellAddress initPosition = getHeaderPosition(sheet, anno, adaptor, config);
        if(initPosition == null) {
            return null;
        }
        
        final int initColumn = initPosition.getColumn();
        final int initRow = initPosition.getRow();
        
        int hColumn = initColumn;
        int hRow = initRow;
        
        // get header columns.
        final List<RecordHeader> headers = new ArrayList<>();
        int rangeCount = 1;
        while(true) {
            try {
                Cell cell = POIUtils.getCell(sheet, hColumn, hRow);
                
                while(POIUtils.isEmptyCellContents(cell, config.getCellFormatter()) && rangeCount < anno.range()) {
                    cell = POIUtils.getCell(sheet, hColumn + rangeCount, hRow);
                    rangeCount++;
                }
                
                final String cellValue = POIUtils.getCellContents(cell, config.getCellFormatter());
                if(Utils.isEmpty(cellValue)){
                    break;
                }
                
                headers.add(new RecordHeader(cellValue, cell.getColumnIndex() - initColumn));
                hColumn = hColumn + rangeCount;
                rangeCount = 1;
                
                // 結合しているセルの場合は、はじめのセルだけ取得して、後は結合分スキップする。
                CellRangeAddress mergedRange = POIUtils.getMergedRegion(sheet, cell.getRowIndex(), cell.getColumnIndex());
                if(mergedRange != null) {
                    hColumn = hColumn + (mergedRange.getLastColumn() - mergedRange.getFirstColumn());
                }
                
            } catch(ArrayIndexOutOfBoundsException ex) {
                break;
            }
            
            if(anno.headerLimit() > 0 && headers.size() >= anno.headerLimit()){
                break;
            }
        }
        
        // データ行の開始位置の調整
        hRow += anno.headerBottom();
        
        return loadRecords(sheet, headers, anno, new CellAddress(hRow, initColumn), 0, adaptor, recordClass, config, work);
        
    }
    
    private List<?> loadRecords(final Sheet sheet, final List<RecordHeader> headers,
            final XlsHorizontalRecords anno, 
            final CellAddress initPosition, final int parentMergedSize,
            final FieldAdaptor adaptor, final Class<?> recordClass, 
            final XlsMapperConfig config, final LoadingWorkObject work) throws XlsMapperException {
        
        final List<Object> result = new ArrayList<>();
        
        final int initColumn = initPosition.getColumn();
        final int initRow = initPosition.getRow();
        
        final int maxRow = initRow + parentMergedSize;
        int hRow = initRow;
        
        // Check for columns
        RecordsProcessorUtil.checkColumns(sheet, recordClass, headers, work.getAnnoReader(), config);
        
        RecordTerminal terminal = anno.terminal();
        if(terminal == null){
            terminal = RecordTerminal.Empty;
        }
        
        final int startHeaderIndex = getStartHeaderIndexForLoading(headers, recordClass, work.getAnnoReader(), config);
        
        // get records
        while(hRow < POIUtils.getRows(sheet)){
            
            if(parentMergedSize > 0 && hRow >= maxRow) {
                // ネストしている処理のとき、最大の処理レコード数をチェックする。
                break;
            }
            
            boolean emptyFlag = true;
            // recordは、マッピング先のオブジェクトのインスタンス。
            final Object record = config.createBean(recordClass);
            
            // パスの位置の変更
            work.getErrors().pushNestedPath(adaptor.getName(), result.size());
            
            // execute PreProcess listener
            final XlsListener listenerAnno = work.getAnnoReader().getAnnotation(record.getClass(), XlsListener.class);
            if(listenerAnno != null) {
                Object listenerObj = config.createBean(listenerAnno.listenerClass());
                for(Method method : listenerObj.getClass().getMethods()) {
                    final XlsPreLoad preProcessAnno = work.getAnnoReader().getAnnotation(listenerAnno.listenerClass(), method, XlsPreLoad.class);
                    if(preProcessAnno != null) {
                        Utils.invokeNeedProcessMethod(listenerObj, method, record, sheet, config, work.getErrors());
                    }
                }
            }
            
            // execute PreProcess method
            for(Method method : record.getClass().getMethods()) {
                final XlsPreLoad preProcessAnno = work.getAnnoReader().getAnnotation(record.getClass(), method, XlsPreLoad.class);
                if(preProcessAnno != null) {
                    Utils.invokeNeedProcessMethod(record, method, record, sheet, config, work.getErrors());
                }
            }
            
            final List<MergedRecord> mergedRecords = new ArrayList<>();
            
            loadMapColumns(sheet, headers, mergedRecords, new CellAddress(hRow, initColumn), record, config, work);
            
            for(int i=0; i < headers.size() && hRow < POIUtils.getRows(sheet); i++){
                final RecordHeader headerInfo = headers.get(i);
                int hColumn = initColumn + headerInfo.getInterval();
                final Cell cell = POIUtils.getCell(sheet, hColumn, hRow);
                
                // find end of the table
                if(!POIUtils.isEmptyCellContents(cell, config.getCellFormatter())){
                    emptyFlag = false;
                }
                
                if(terminal == RecordTerminal.Border && i == startHeaderIndex){
                    final CellStyle format = cell.getCellStyle();
                    if(format != null && !(format.getBorderLeft() == CellStyle.BORDER_NONE)){
                        emptyFlag = false;
                    } else {
                        emptyFlag = true;
                        break;
                    }
                }
                
                if(!anno.terminateLabel().equals("")){
                    if(Utils.matches(POIUtils.getCellContents(cell, config.getCellFormatter()), anno.terminateLabel(), config)){
                        emptyFlag = true;
                        break;
                    }
                }
                
                // mapping from Excel columns to Object properties.
                final List<FieldAdaptor> propeties = Utils.getLoadingColumnProperties(
                        record.getClass(), headerInfo.getLabel(), work.getAnnoReader(), config);
                for(FieldAdaptor property : propeties) {
                    Cell valueCell = cell;
                    final XlsColumn column = property.getLoadingAnnotation(XlsColumn.class);
                    if(column.headerMerged() > 0) {
                        hColumn = hColumn + column.headerMerged();
                        valueCell = POIUtils.getCell(sheet, hColumn, hRow);
                    }
                    
                    // for merged cell
                    if(POIUtils.isEmptyCellContents(valueCell, config.getCellFormatter())) {
                        final CellStyle valueCellFormat = valueCell.getCellStyle();
                        if(column.merged()
                                && (valueCellFormat == null || valueCellFormat.getBorderTop() == CellStyle.BORDER_NONE)) {
                            for(int k=hRow-1; k > initRow; k--){
                                Cell tmpCell = POIUtils.getCell(sheet, hColumn, k);
                                final CellStyle tmpCellFormat = tmpCell.getCellStyle();
                                if(tmpCellFormat!=null && !(tmpCellFormat.getBorderBottom() == CellStyle.BORDER_NONE)){
                                    break;
                                }
                                if(!POIUtils.isEmptyCellContents(tmpCell, config.getCellFormatter())){
                                    valueCell = tmpCell;
                                    break;
                                }
                            }
                        }
                    }
                    
                    if(column.headerMerged() > 0){
                        hColumn = hColumn - column.headerMerged();
                    }
                    
                    CellRangeAddress mergedRange = POIUtils.getMergedRegion(sheet, valueCell.getRowIndex(), valueCell.getColumnIndex());
                    if(mergedRange != null) {
                        int mergedSize =  mergedRange.getLastRow() - mergedRange.getFirstRow() + 1;
                        mergedRecords.add(new MergedRecord(headerInfo, mergedRange, mergedSize));
                    } else {
                        mergedRecords.add(new MergedRecord(headerInfo, CellRangeAddress.valueOf(Utils.formatCellAddress(valueCell)), 1));
                    }
                    
                    // set for value
                    Utils.setPosition(valueCell.getColumnIndex(), valueCell.getRowIndex(), record, property.getName());
                    Utils.setLabel(headerInfo.getLabel(), record, property.getName());
                    final CellConverter<?> converter = getLoadingCellConverter(property, config.getConverterRegistry(), config);
                    try {
                        final Object value = converter.toObject(valueCell, property, config);
                        property.setValue(record, value);
                    } catch(TypeBindException e) {
                        work.addTypeBindError(e, valueCell, property.getName(), headerInfo.getLabel());
                        if(!config.isContinueTypeBindFailure()) {
                            throw e;
                        }
                    }
                }
                
            }
            
            // execute nested record
            final int skipSize = loadNestedRecords(sheet, headers, mergedRecords, anno, new CellAddress(hRow, initColumn), record, config, work);
            if(parentMergedSize > 0 && skipSize > 0 && (hRow + skipSize) > maxRow) {
                // check over merged cell.
                String message = String.format("Over merged size. In sheet '%s' with rowIndex=%d, over the rowIndex=%s.",
                        sheet.getSheetName(), hRow + skipSize, maxRow);
                throw new NestMergedSizeException(sheet.getSheetName(), skipSize, message);
            }
            
            if(emptyFlag){
                // パスの位置の変更
                work.getErrors().popNestedPath();
                break;
            }
            
            if(!anno.ignoreEmptyRecord() || !isEmptyRecord(record, work.getAnnoReader())) {
                result.add(record);
                
            }
            
            // set PostProcess listener
            if(listenerAnno != null) {
                Object listenerObj = config.createBean(listenerAnno.listenerClass());
                for(Method method : listenerObj.getClass().getMethods()) {
                    
                    final XlsPostLoad postProcessAnno = work.getAnnoReader().getAnnotation(listenerAnno.listenerClass(), method, XlsPostLoad.class);
                    if(postProcessAnno != null) {
                        work.addNeedPostProcess(new NeedProcess(record, listenerObj, method));
                    }
                }
            }
            
            // set PostProcess method
            for(Method method : record.getClass().getMethods()) {
                final XlsPostLoad postProcessAnno = work.getAnnoReader().getAnnotation(record.getClass(), method, XlsPostLoad.class);
                if(postProcessAnno != null) {
                    work.addNeedPostProcess(new NeedProcess(record, record, method));
                }
            }
            
            // パスの位置の変更
            work.getErrors().popNestedPath();
            
            if(skipSize > 0) {
                hRow += skipSize;
            } else {
                hRow++;
            }
        }
        
        return result;
    }
    
    /**
     * 表の開始位置（見出し）の位置情報を取得する。
     * 
     * @param sheet
     * @param anno
     * @param adaptor
     * @param config
     * @return 表の開始位置。指定したラベルが見つからない場合、設定によりnullを返す。
     * @throws AnnotationInvalidException アノテーションの値が不正で、表の開始位置が位置が見つからない場合。
     * @throws CellNotFoundException 指定したラベルが見つからない場合。
     */
    private CellAddress getHeaderPosition(final Sheet sheet, final XlsHorizontalRecords anno,
            final FieldAdaptor adaptor, final XlsMapperConfig config) throws AnnotationInvalidException, CellNotFoundException {
        
        if(Utils.isNotEmpty(anno.headerAddress())) {
            final Point address = Utils.parseCellAddress(anno.headerAddress());
            if(address == null) {
                throw new AnnotationInvalidException(
                        String.format("With '%s, @XlsHorizontalRecords#headerAddress is wrong cell address '%s'.",
                                adaptor.getNameWithClass(), anno.headerAddress()), anno);
            }
            
            return new CellAddress(address);
            
        } else if(Utils.isNotEmpty(anno.tableLabel())) {
            try {
                Cell labelCell = Utils.getCell(sheet, anno.tableLabel(), 0, 0, config);
                int initColumn = labelCell.getColumnIndex();
                int initRow = labelCell.getRowIndex() + anno.bottom();
                
                return new CellAddress(initRow, initColumn);
                
            } catch(CellNotFoundException ex) {
                if(anno.optional()) {
                    return null;
                } else {
                    throw ex;
                }
            }
            
        } else {
            // column, rowのアドレスを直接指定の場合
            if(anno.headerColumn() < 0 || anno.headerRow() < 0) {
                throw new AnnotationInvalidException(
                        String.format("With '%s', @XlsHorizontalRecors#headerColumn or headerRow should be greater than or equal zero. (headerColumn=%d, headerRow=%d)",
                                adaptor.getNameWithClass(), anno.headerColumn(), anno.headerRow()), anno);
            }
            
            return new CellAddress(anno.headerRow(), anno.headerColumn());
        }
        
    }
    
    /**
     * 表の見出しから、レコードのJavaクラスの定義にあるカラムの定義で初めて見つかるリストのインデックスを取得する。
     * <p>カラムの定義とは、アノテーション「@XlsColumn」が付与されたもの。
     * @param headers 表の見出し情報。
     * @param recordClass アノテーション「@XlsColumn」が定義されたフィールドを持つレコード用のクラス。
     * @param annoReader {@link AnnotationReader}
     * @param config システム設定
     * @return 引数「headers」の該当する要素のインデックス番号。不明な場合は0を返す。
     */
    private int getStartHeaderIndexForLoading(final List<RecordHeader> headers, Class<?> recordClass, 
            final AnnotationReader annoReader, final XlsMapperConfig config) {
        
        // レコードクラスが不明の場合、0を返す。
        if((recordClass == null || recordClass.equals(Object.class))) {
            return 0;
        }
        
        for(int i=0; i < headers.size(); i++) {
            RecordHeader headerInfo = headers.get(i);
            final List<FieldAdaptor> propeties = Utils.getLoadingColumnProperties(
                    recordClass, headerInfo.getLabel(), annoReader, config);
            if(!propeties.isEmpty()) {
                return i;
            }
        }
        
        return 0;
        
    }
    
    private void loadMapColumns(final Sheet sheet, final List<RecordHeader> headers, final List<MergedRecord> mergedRecords,
            final CellAddress beginPosition, final Object record, final XlsMapperConfig config, final LoadingWorkObject work) throws XlsMapperException {
        
        final List<FieldAdaptor> properties = Utils.getLoadingMapColumnProperties(record.getClass(), work.getAnnoReader());
        
        for(FieldAdaptor property : properties) {
            final XlsMapColumns mapAnno = property.getLoadingAnnotation(XlsMapColumns.class);
            
            Class<?> itemClass = mapAnno.itemClass();
            if(itemClass == Object.class) {
                itemClass = property.getLoadingGenericClassType();
            }
            
            // get converter (map key class)
            final CellConverter<?> converter = config.getConverterRegistry().getConverter(itemClass);
            if(converter == null) {
                throw newNotFoundConverterExpcetion(itemClass);
            }
            
            boolean foundPreviousColumn = false;
            final Map<String, Object> map = new LinkedHashMap<>();
            for(RecordHeader headerInfo : headers) {
                int hColumn = beginPosition.getColumn() + headerInfo.getInterval();
                if(Utils.matches(headerInfo.getLabel(), mapAnno.previousColumnName(), config)){
                    foundPreviousColumn = true;
                    hColumn++;
                    continue;
                }
                
                if(Utils.isNotEmpty(mapAnno.nextColumnName()) && Utils.matches(headerInfo.getLabel(), mapAnno.nextColumnName(), config)) {
                    break;
                }
                
                if(foundPreviousColumn){
                    final Cell cell = POIUtils.getCell(sheet, hColumn, beginPosition.getRow());
                    Utils.setPositionWithMapColumn(cell.getColumnIndex(), cell.getRowIndex(), record, property.getName(), headerInfo.getLabel());
                    Utils.setLabelWithMapColumn(headerInfo.getLabel(), record, property.getName(), headerInfo.getLabel());
                    
                    CellRangeAddress mergedRange = POIUtils.getMergedRegion(sheet, cell.getRowIndex(), cell.getColumnIndex());
                    if(mergedRange != null) {
                        int mergedSize =  mergedRange.getLastRow() - mergedRange.getFirstRow() + 1;
                        mergedRecords.add(new MergedRecord(headerInfo, mergedRange, mergedSize));
                    } else {
                        mergedRecords.add(new MergedRecord(headerInfo, CellRangeAddress.valueOf(Utils.formatCellAddress(cell)), 1));
                    }
                    
                    try {
                        final Object value = converter.toObject(cell, property, config);
                        map.put(headerInfo.getLabel(), value);
                    } catch(TypeBindException e) {
                        e.setBindClass(itemClass);  // マップの項目のタイプに変更
                        work.addTypeBindError(e, cell, String.format("%s[%s]", property.getName(), headerInfo.getLabel()), headerInfo.getLabel());
                        if(!config.isContinueTypeBindFailure()) {
                            throw e;
                        } 
                    }
                }
            }
            
            property.setValue(record, map);
        }
    }
    
    @SuppressWarnings("unchecked")
    private int loadNestedRecords(final Sheet sheet, final List<RecordHeader> headers, final List<MergedRecord> mergedRecords,
            final XlsHorizontalRecords anno,
            final CellAddress beginPosition, 
            final Object record,
            final XlsMapperConfig config, final LoadingWorkObject work) throws XlsMapperException {
        
        // 読み飛ばす、レコード数。
        // 基本的に結合している個数による。
        int skipSize = 0;
        
        final List<FieldAdaptor> nestedProperties = Utils.getLoadingNestedRecordsProperties(record.getClass(), work.getAnnoReader());
        for(FieldAdaptor property : nestedProperties) {
            
            final XlsNestedRecords nestedAnno = property.getLoadingAnnotation(XlsNestedRecords.class);
            final Class<?> clazz = property.getTargetClass();
            if(Collection.class.isAssignableFrom(clazz)) {
                
                // mapping by one-to-many
                
                int mergedSize = RecordsProcessorUtil.checkNestedMergedSizeRecords(sheet, mergedRecords);
                if(skipSize < mergedSize) {
                    skipSize = mergedSize;
                }
                
                Class<?> recordClass = nestedAnno.recordClass();
                if(recordClass == Object.class) {
                    recordClass = property.getLoadingGenericClassType();
                }
                
                List<?> value = loadRecords(sheet, headers, anno, beginPosition, mergedSize, property, recordClass, config, work);
                if(value != null) {
                    Collection<?> collection = Utils.convertListToCollection(value, (Class<Collection>)clazz, config.getBeanFactory());
                    property.setValue(record, collection);
                }
                
            } else if(clazz.isArray()) {
                
                // mapping by one-to-many
                
                int mergedSize = RecordsProcessorUtil.checkNestedMergedSizeRecords(sheet, mergedRecords);
                if(skipSize < mergedSize) {
                    skipSize = mergedSize;
                }
                
                Class<?> recordClass = anno.recordClass();
                if(recordClass == Object.class) {
                    recordClass = property.getLoadingGenericClassType();
                }
                
                List<?> value = loadRecords(sheet, headers, anno, beginPosition, mergedSize, property, recordClass, config, work);
                if(value != null) {
                    final Object array = Array.newInstance(recordClass, value.size());
                    for(int i=0; i < value.size(); i++) {
                        Array.set(array, i, value.get(i));
                    }
                    
                    property.setValue(record, array);
                }
                
            } else {
                // mapping by one-to-tone
                
                int mergedSize = 1;
                if(skipSize < mergedSize) {
                    skipSize = mergedSize;
                }
                
                Class<?> recordClass = anno.recordClass();
                if(recordClass == Object.class) {
                    recordClass = property.getTargetClass();
                }
                
                List<?> value = loadRecords(sheet, headers, anno, beginPosition, mergedSize, property, recordClass, config, work);
                if(value != null && !value.isEmpty()) {
                    property.setValue(record, value.get(0));
                }
                
            }
        }
        
        return skipSize;
    }
    
    /**
     * レコードの値か空かどうか判定する。
     * <p>アノテーション<code>@XlsIsEmpty</code>のメソッドで判定を行う。
     * @param record
     * @param annoReader
     * @return アノテーションがない場合はfalseを返す。
     * @throws AnnotationReadException 
     * @throws AnnotationInvalidException 
     */
    private boolean isEmptyRecord(final Object record, final AnnotationReader annoReader) throws AnnotationReadException, AnnotationInvalidException {
        
        for(Method method : record.getClass().getMethods()) {
            final XlsIsEmpty emptyAnno = annoReader.getAnnotation(record.getClass(), method, XlsIsEmpty.class);
            if(emptyAnno == null) {
                continue;
            }
            
            try {
                method.setAccessible(true);
                return (boolean) method.invoke(record);
            } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
                throw new AnnotationInvalidException(
                        String.format("@XlsIsEmpty should be appended method that no args and returning boolean type."),
                        emptyAnno);
            }
        }
        
        // メソッドが見つからない場合。
        return false;
    }
    
    @Override
    public void saveProcess(final Sheet sheet, final Object beansObj, final XlsHorizontalRecords anno,
            final FieldAdaptor adaptor, final XlsMapperConfig config, final SavingWorkObject work) throws XlsMapperException {
        
        // ラベルの設定
        if(Utils.isNotEmpty(anno.tableLabel())) {
            try {
                final Cell tableLabelCell = Utils.getCell(sheet, anno.tableLabel(), 0, config);
                Utils.setLabel(POIUtils.getCellContents(tableLabelCell, config.getCellFormatter()), beansObj, adaptor.getName());
            } catch(CellNotFoundException e) {
                
            }
        }
        
        final Class<?> clazz = adaptor.getTargetClass();
        final Object result = adaptor.getValue(beansObj);
        if(Collection.class.isAssignableFrom(clazz)) {
            
            Class<?> recordClass = anno.recordClass();
            if(recordClass == Object.class) {
                recordClass = adaptor.getSavingGenericClassType();
            }
            
            final Collection<Object> value = (result == null ? new ArrayList<Object>() : (Collection<Object>) result);
            final List<Object> list = Utils.convertCollectionToList(value);
            saveRecords(sheet, anno, adaptor, recordClass, list, config, work);
            
        } else if(clazz.isArray()) {
            
            Class<?> recordClass = anno.recordClass();
            if(recordClass == Object.class) {
                recordClass = adaptor.getSavingGenericClassType();
            }
            
            final List<Object> list = (result == null ? new ArrayList<Object>() : Arrays.asList((Object[]) result));
            saveRecords(sheet, anno, adaptor, recordClass, list, config, work);
            
        } else {
            throw new AnnotationInvalidException(
                    String.format("With '%s', annotation '@XlsHorizontalRecords' should only granted Collection(List/Set) or array. : %s", 
                            adaptor.getNameWithClass(), clazz.getName()),
                            anno);
        }
        
    }
    
    private void saveRecords(final Sheet sheet, final XlsHorizontalRecords anno, final FieldAdaptor adaptor, 
            final Class<?> recordClass, final List<Object> result, final XlsMapperConfig config, final SavingWorkObject work) throws XlsMapperException {
        
        RecordsProcessorUtil.checkSavingNestedRecordClass(recordClass, adaptor, work.getAnnoReader());
        
        // get table starting position
        final CellAddress initPosition = getHeaderPosition(sheet, anno, adaptor, config);
        if(initPosition == null) {
            return;
        }
        
        int initColumn = initPosition.getColumn();
        int initRow = initPosition.getRow();
        
        int hColumn = initColumn;
        int hRow = initRow;
        
        // get header columns.
        final List<RecordHeader> headers = new ArrayList<>();
        int rangeCount = 1;
        while(true) {
            try {
                Cell cell = POIUtils.getCell(sheet, hColumn, hRow);
                while(POIUtils.isEmptyCellContents(cell, config.getCellFormatter()) && rangeCount < anno.range()) {
                    cell = POIUtils.getCell(sheet, hColumn + rangeCount, hRow);
                    rangeCount++;
                }
                
                String cellValue = POIUtils.getCellContents(cell, config.getCellFormatter());
                if(Utils.isEmpty(cellValue)){
                    break;
                }
                
                headers.add(new RecordHeader(cellValue, cell.getColumnIndex() - initColumn));
                hColumn = hColumn + rangeCount;
                rangeCount = 1;
                
                // 結合しているセルの場合は、はじめのセルだけ取得して、後は結合分スキップする。
                CellRangeAddress mergedRange = POIUtils.getMergedRegion(sheet, cell.getRowIndex(), cell.getColumnIndex());
                if(mergedRange != null) {
                    hColumn = hColumn + (mergedRange.getLastColumn() - mergedRange.getFirstColumn());
                }
                
            } catch(ArrayIndexOutOfBoundsException ex) {
                break;
            }
            
            if(anno.headerLimit() > 0 && headers.size() >= anno.headerLimit()){
                break;
            }
        }

        // XlsColumn(merged=true)の結合したセルの情報
        final List<CellRangeAddress> mergedRanges = new ArrayList<>();
        
        // 書き込んだセルの範囲などの情報
        final RecordOperation recordOperation = new RecordOperation();
        recordOperation.setupCellPositoin(hRow+1, initColumn);
        
        // コメントの補完
        final List<CellCommentStore> commentStoreList;
        if(config.isCorrectCellCommentOnSave()
                && (anno.overRecord().equals(OverRecordOperate.Insert) || anno.remainedRecord().equals(RemainedRecordOperate.Delete))) {
            commentStoreList = loadCommentAndRemove(sheet);
        } else {
            commentStoreList = new ArrayList<>();
        }
        
        // データ行の開始位置の調整
        hRow += anno.headerBottom();
        
        saveRecords(sheet, headers, anno, new CellAddress(hRow, initColumn), new AtomicInteger(0), adaptor, recordClass, result, config,
                work, mergedRanges, recordOperation, new ArrayList<Integer>());
        
        // 書き込むデータがない場合は、1行目の終端を操作範囲とする。
        if(result.isEmpty()) {
            recordOperation.setupCellPositoin(hRow-2, hColumn-1);
        }
        
        if(config.isCorrectCellDataValidationOnSave()) {
            correctDataValidation(sheet, recordOperation);
        }
        
        if(config.isCorrectNameRangeOnSave()) {
            correctNameRange(sheet, recordOperation);
        }
        
        if(config.isCorrectCellCommentOnSave()) {
            correctComment(sheet, recordOperation, commentStoreList);
        }
        
    }
    
    private void saveRecords(final Sheet sheet, final List<RecordHeader> headers,
            final XlsHorizontalRecords anno,
            final CellAddress initPosition, final AtomicInteger nestedRecordSize,
            final FieldAdaptor adaptor, final Class<?> recordClass, final List<Object> result,
            final XlsMapperConfig config, final SavingWorkObject work,
            final List<CellRangeAddress> mergedRanges, final RecordOperation recordOperation,
            final List<Integer> inserteRowsIdx) throws XlsMapperException {
        
        final int initColumn = initPosition.getColumn();
        final int initRow = initPosition.getRow();
        
        int hRow = initRow;
        
        // Check for columns
        RecordsProcessorUtil.checkColumns(sheet, recordClass, headers, work.getAnnoReader(), config);
        
        /*
         * 書き込む時には終了位置の判定は、Borderで固定する必要がある。
         * ・Emptyの場合だと、テンプレート用のシートなので必ずデータ用のセルが、空なので書き込まれなくなる。
         * ・Emptyの場合、Borderに補正して書き込む。
         */
        RecordTerminal terminal = anno.terminal();
        if(terminal == RecordTerminal.Empty) {
            terminal = RecordTerminal.Border;
        } else if(terminal == null){
            terminal = RecordTerminal.Border;
        }
        
        
        final int startHeaderIndex = getStartHeaderIndexForSaving(headers, recordClass, work.getAnnoReader(), config);
        
        // get records
        for(int r=0; r < POIUtils.getRows(sheet); r++) {
            
            boolean emptyFlag = true;
            
            // 書き込むレコードのオブジェクトを取得。データが0件の場合、nullとなる。
            Object record = null;
            if(r < result.size()) {
                record = result.get(r);
            }
            
            // パスの位置の変更
            work.getErrors().pushNestedPath(adaptor.getName(), r);
            
            if(record != null) {
                
                // execute PreProcess/ listner
                final XlsListener listenerAnno = work.getAnnoReader().getAnnotation(record.getClass(), XlsListener.class);
                if(listenerAnno != null) {
                    Object listenerObj = config.createBean(listenerAnno.listenerClass());
                    for(Method method : listenerObj.getClass().getMethods()) {
                        final XlsPreSave preProcessAnno = work.getAnnoReader().getAnnotation(listenerAnno.listenerClass(), method, XlsPreSave.class);
                        if(preProcessAnno != null) {
                            Utils.invokeNeedProcessMethod(listenerObj, method, record, sheet, config, work.getErrors());
                        }
                    }
                }
                
                // execute PreProcess/PostProcess method
                for(Method method : record.getClass().getMethods()) {
                    final XlsPreSave preProcessAnno = work.getAnnoReader().getAnnotation(record.getClass(), method, XlsPreSave.class);
                    if(preProcessAnno != null) {
                        Utils.invokeNeedProcessMethod(record, method, record, sheet, config, work.getErrors());                    
                    }
                }
            }
            
            // レコードの各列処理で既に行を追加したかどうかのフラグ。(ネスト先でも参照する)
            boolean insertRows = inserteRowsIdx.contains(hRow+1);
            
            // レコードの各列処理で既に行を削除したかどうかのフラグ。
            boolean deleteRows = false;
            
            // 書き込んだセルの座標
            // ネストしたときに、結合するための情報として使用する。
            List<CellAddress> valueCellPositions = new ArrayList<>();
            
            // hRowという上限がない
            for(int i=0; i < headers.size(); i++) {
                final RecordHeader headerInfo = headers.get(i);
                int hColumn = initColumn + headerInfo.getInterval();
                final Cell cell = POIUtils.getCell(sheet, hColumn, hRow);
                
                // find end of the table
                if(!POIUtils.isEmptyCellContents(cell, config.getCellFormatter())){
                    emptyFlag = false;
                }
                
                if(terminal == RecordTerminal.Border && i == startHeaderIndex){
                    final CellStyle format = cell.getCellStyle();
                    if(format != null && !(format.getBorderLeft() == CellStyle.BORDER_NONE)){
                        emptyFlag = false;
                    } else {
                        emptyFlag = true;
//                            break;
                    }
                }
                
                if(!anno.terminateLabel().equals("")){
                    if(Utils.matches(POIUtils.getCellContents(cell, config.getCellFormatter()), anno.terminateLabel(), config)){
                        emptyFlag = true;
//                            break;
                    }
                }
                
                // mapping from Excel columns to Object properties.
                if(record != null) {
                    final List<FieldAdaptor> propeties = Utils.getSavingColumnProperties(
                            record.getClass(), headerInfo.getLabel(), work.getAnnoReader(), config);
                    for(FieldAdaptor property : propeties) {
                        Cell valueCell = cell;
                        final XlsColumn column = property.getSavingAnnotation(XlsColumn.class);
                        
                        //TODO: マージを考慮する必要はないかも
                        if(column.headerMerged() > 0) {
                            hColumn = hColumn + column.headerMerged();
                            valueCell = POIUtils.getCell(sheet, hColumn, hRow);
                        }
                        
                        // for merged cell
                        if(POIUtils.isEmptyCellContents(valueCell, config.getCellFormatter())) {
                            final CellStyle valueCellFormat = valueCell.getCellStyle();
                            if(column.merged()
                                    && (valueCellFormat == null || valueCellFormat.getBorderTop() == CellStyle.BORDER_NONE)) {
                                for(int k=hRow-1; k > initRow; k--){
                                    Cell tmpCell = POIUtils.getCell(sheet, hColumn, k);
                                    final CellStyle tmpCellFormat = tmpCell.getCellStyle();
                                    if(tmpCellFormat != null && !(tmpCellFormat.getBorderBottom() == CellStyle.BORDER_NONE)){
                                        break;
                                    }
                                    if(!POIUtils.isEmptyCellContents(tmpCell, config.getCellFormatter())){
                                        valueCell = tmpCell;
                                        break;
                                    }
                                }
                            }
                        }
                        
                        if(column.headerMerged() > 0){
                            hColumn = hColumn - column.headerMerged();
                        }
                        
                        // 書き込む行が足りない場合の操作
                        if(emptyFlag) {
                            if(anno.overRecord().equals(OverRecordOperate.Break)) {
                                break;
                                
                            } else if(anno.overRecord().equals(OverRecordOperate.Copy)) {
                                // 1つ上のセルの書式をコピーする。
                                final CellStyle style = POIUtils.getCell(sheet, valueCell.getColumnIndex(), valueCell.getRowIndex()-1).getCellStyle();
                                valueCell.setCellStyle(style);
                                valueCell.setCellType(Cell.CELL_TYPE_BLANK);
                                
                                recordOperation.incrementCopyRecord();
                                
                            } else if(anno.overRecord().equals(OverRecordOperate.Insert)) {
                                // すでに他の列の処理に対して行を追加している場合は行の追加は行わない。
                                if(!insertRows) {
                                    // 行を下に追加する
                                    POIUtils.insertRow(sheet, valueCell.getRowIndex()+1);
                                    insertRows = true;
                                    recordOperation.incrementInsertRecord();
                                    inserteRowsIdx.add(valueCell.getRowIndex()+1);
                                    
                                    if(logger.isDebugEnabled()) {
                                        logger.debug("insert row : sheet name=[{}], row index=[{}]", sheet.getSheetName(), valueCell.getRowIndex()+1);
                                    }
                                }
                                
                                // １つ上のセルの書式をコピーする
                                final CellStyle style = POIUtils.getCell(sheet, valueCell.getColumnIndex(), valueCell.getRowIndex()-1).getCellStyle();
                                valueCell.setCellStyle(style);
                                valueCell.setCellType(Cell.CELL_TYPE_BLANK);
                            }
                            
                        }
                        
                        valueCellPositions.add(new CellAddress(valueCell));
                        
                        // set for cell value
                        Utils.setPosition(valueCell.getColumnIndex(), valueCell.getRowIndex(), record, property.getName());
                        Utils.setLabel(headerInfo.getLabel(), record, property.getName());
                        final CellConverter converter = getSavingCellConverter(property, config.getConverterRegistry(), config);
                        try {
                            converter.toCell(property, property.getValue(record), sheet, valueCell.getColumnIndex(), valueCell.getRowIndex(), config);
                        } catch(TypeBindException e) {
                            work.addTypeBindError(e, valueCell, property.getName(), headerInfo.getLabel());
                            if(!config.isContinueTypeBindFailure()) {
                                throw e;
                            }   
                        }
                        
                        recordOperation.setupCellPositoin(valueCell);
                        
                        // セルをマージする
                        if(column.merged() && (r > 0) && config.isMergeCellOnSave()) {
                            processSavingMergedCell(valueCell, sheet, mergedRanges, config);
                        }
                    }
                }
                
                /*
                 * 残りの行の操作
                 *  行の追加やコピー処理をしていないときのみ実行する
                 */
                if(record == null && emptyFlag == false && recordOperation.isNotExecuteOverRecordOperation()) {
                    if(anno.remainedRecord().equals(RemainedRecordOperate.None)) {
                        // なにもしない
                        
                    } else if(anno.remainedRecord().equals(RemainedRecordOperate.Clear)) {
                        final Cell clearCell = POIUtils.getCell(sheet, hColumn, hRow);
                        clearCell.setCellType(Cell.CELL_TYPE_BLANK);
                        
                    } else if(anno.remainedRecord().equals(RemainedRecordOperate.Delete)) {
                        if(initRow == hRow -1) {
                            // 1行目は残しておき、値をクリアする
                            final Cell clearCell = POIUtils.getCell(sheet, hColumn, hRow);
                            clearCell.setCellType(Cell.CELL_TYPE_BLANK);
                            
                        } else if(!deleteRows) {
                            // すでに他の列の処理に対して行を削除している場合は行の削除は行わない。
                            final Row row = POIUtils.removeRow(sheet, hRow);
                            deleteRows = true;
                            
                            if(row != null) {
                                if(logger.isDebugEnabled()) {
                                    logger.debug("delete row : sheet name=[{}], row index=[{}]", sheet.getSheetName(), hRow);
                                }
                                recordOperation.incrementDeleteRecord();
                            }
                        }
                    }
                }
                
            }
            
            // マップ形式のカラムを出力する
            if(record != null) {
                saveMapColumn(sheet, headers, valueCellPositions, new CellAddress(hRow, initColumn), record, terminal, anno, config, work, recordOperation);
            }
            
            // execute nested record.
            int skipSize = 0;
            if(record != null) {
                skipSize = saveNestedRecords(sheet, headers, valueCellPositions, anno, new CellAddress(hRow, initColumn), record,
                        config, work, mergedRanges, recordOperation, inserteRowsIdx);
                nestedRecordSize.addAndGet(skipSize);
            }
            
            if(record != null) {
                
                // set PostProcess listener
                final XlsListener listenerAnno = work.getAnnoReader().getAnnotation(record.getClass(), XlsListener.class);
                if(listenerAnno != null) {
                    Object listenerObj = config.createBean(listenerAnno.listenerClass());
                    for(Method method : listenerObj.getClass().getMethods()) {
                        
                        final XlsPostSave postProcessAnno = work.getAnnoReader().getAnnotation(listenerAnno.listenerClass(), method, XlsPostSave.class);
                        if(postProcessAnno != null) {
                            work.addNeedPostProcess(new NeedProcess(record, listenerObj, method));
                        }
                    }
                }
                
                // set PostProcess method
                for(Method method : record.getClass().getMethods()) {
                    
                    final XlsPostSave postProcessAnno = work.getAnnoReader().getAnnotation(record.getClass(), method, XlsPostSave.class);
                    if(postProcessAnno != null) {
                        work.addNeedPostProcess(new NeedProcess(record, record, method));
                    }
                }
                
            }
            
            // パスの位置の変更
            work.getErrors().popNestedPath();
            
            /*
             * 行が削除されていない場合は、次の行に進む。
             * ・行が削除されていると、現在の行数は変わらない。
             */
            if(!deleteRows) {
                if(skipSize > 0) {
                    hRow += skipSize;
                } else {
                    hRow++;
                }
            }
            
            if(emptyFlag == true && (r > result.size())) {
                // セルが空で、書き込むデータがない場合。
                break;
            }
        }
        
        
    }
    
    /**
     * 表の見出しから、レコードのJavaクラスの定義にあるカラムの定義で初めて見つかるリストのインデックスを取得する。
     * ・カラムの定義とは、アノテーション「@XlsColumn」が付与されたもの。
     * @param headers 表の見出し情報。
     * @param recordClass アノテーション「@XlsColumn」が定義されたフィールドを持つレコード用のクラス。
     * @param annoReader AnnotationReader
     * @param config システム設定
     * @return 引数「headers」の該当する要素のインデックス番号。不明な場合は、0を返す。
     */
    private int getStartHeaderIndexForSaving(final List<RecordHeader> headers, Class<?> recordClass,
            final AnnotationReader annoReader, final XlsMapperConfig config) {
        
        // レコードクラスが不明の場合、0を返す。
        if((recordClass == null || recordClass.equals(Object.class))) {
            return 0;
        }
        
        for(int i=0; i < headers.size(); i++) {
            RecordHeader headerInfo = headers.get(i);
            final List<FieldAdaptor> propeties = Utils.getSavingColumnProperties(
                    recordClass, headerInfo.getLabel(), annoReader, config);
            if(!propeties.isEmpty()) {
                return i;
            }
        }
        
        return 0;
        
    }
    
    /**
     * 上部のセルと同じ値の場合マージする
     * @param currentCell
     * @param sheet
     * @param mergedRanges
     * @param config
     * @return
     */
    private boolean processSavingMergedCell(final Cell currentCell, final Sheet sheet,
            final List<CellRangeAddress> mergedRanges, final XlsMapperConfig config) {
        
        final int row = currentCell.getRowIndex();
        final int column = currentCell.getColumnIndex();
        
        if(row <= 0) {
            return false;
        }
        
        // 上のセルと比較する
        final String value = POIUtils.getCellContents(currentCell, config.getCellFormatter());
        String upperValue = POIUtils.getCellContents(POIUtils.getCell(sheet, column, row-1), config.getCellFormatter());
        
        // 結合されている場合、結合の先頭セルを取得する
        int startRow = row - 1;
        CellRangeAddress currentMergedRange = null;
        for(CellRangeAddress range : mergedRanges) {
            // 列が範囲外の場合
            if((range.getFirstColumn() > column) || (column > range.getLastColumn())) {
                continue;
            }
            
            // 行が範囲外の場合
            if((range.getFirstRow() > startRow) || (startRow > range.getLastRow())) {
                continue;
            }
            
            upperValue = POIUtils.getCellContents(POIUtils.getCell(sheet, column, range.getFirstRow()), config.getCellFormatter());
            currentMergedRange = range;
            break;
        }
        
        if(!value.equals(upperValue)) {
            // 値が異なる場合は結合しない
            return false;
        }
        
        // 既に結合済みの場合は一端解除する
        if(currentMergedRange != null) {
            startRow = currentMergedRange.getFirstRow();
            mergedRanges.remove(currentMergedRange);
            POIUtils.removeMergedRange(sheet, currentMergedRange);
        }
        
        final CellRangeAddress newRange = POIUtils.mergeCells(sheet, column, startRow, column, row);
        mergedRanges.add(newRange);
        return true;
        
    }
    
    private void saveMapColumn(final Sheet sheet, final List<RecordHeader> headers, final List<CellAddress> valueCellPositions,
            final CellAddress beginPosition, Object record, RecordTerminal terminal,
            XlsHorizontalRecords anno, XlsMapperConfig config, SavingWorkObject work,
            RecordOperation recordOperation) throws XlsMapperException {
        
        final List<FieldAdaptor> properties = Utils.getSavingMapColumnProperties(record.getClass(), work.getAnnoReader());
        for(FieldAdaptor property : properties) {
            
            final XlsMapColumns mapAnno = property.getSavingAnnotation(XlsMapColumns.class);
            
            Class<?> itemClass = mapAnno.itemClass();
            if(itemClass == Object.class) {
                itemClass = property.getSavingGenericClassType();
            }
            
            // get converter (map key class)
            final CellConverter converter = config.getConverterRegistry().getConverter(itemClass);
            if(converter == null) {
                throw newNotFoundConverterExpcetion(itemClass);
            }
            
            boolean foundPreviousColumn = false;
            for(RecordHeader headerInfo : headers) {
                int hColumn = beginPosition.getColumn() + headerInfo.getInterval();
                if(Utils.matches(headerInfo.getLabel(), mapAnno.previousColumnName(), config)){
                    foundPreviousColumn = true;
                    hColumn++;
                    continue;
                }
                
                if(Utils.isNotEmpty(mapAnno.nextColumnName()) && Utils.matches(headerInfo.getLabel(), mapAnno.nextColumnName(), config)) {
                    break;
                }
                
                if(foundPreviousColumn) {
                    final Cell cell = POIUtils.getCell(sheet, hColumn, beginPosition.getRow());
                    
                    // 空セルか判断する
                    boolean emptyFlag = true;
                    if(terminal == RecordTerminal.Border) {
                        CellStyle format = cell.getCellStyle();
                        if(format != null && !(format.getBorderLeft() == CellStyle.BORDER_NONE)) {
                            emptyFlag = false;
                        } else {
                            emptyFlag = true;
                        }
                    }
                    
                    if(!anno.terminateLabel().equals("")) {
                        if(Utils.matches(POIUtils.getCellContents(cell, config.getCellFormatter()), anno.terminateLabel(), config)) {
                            emptyFlag = true;
                        }
                    }
                    
                    // 空セルの場合
                    if(emptyFlag) {
                        if(anno.overRecord().equals(OverRecordOperate.Break)) {
                            break;
                            
                        } else if(anno.overRecord().equals(OverRecordOperate.Copy)) {
                            final CellStyle style = POIUtils.getCell(sheet, cell.getColumnIndex(), cell.getRowIndex()-1).getCellStyle();
                            cell.setCellStyle(style);
                            cell.setCellType(Cell.CELL_TYPE_BLANK);
                            
                        } else if(anno.overRecord().equals(OverRecordOperate.Insert)) {
                            // 既に追加ずみなので、セルの書式のコピーのみ行う
                            final CellStyle style = POIUtils.getCell(sheet, cell.getColumnIndex(), cell.getRowIndex()-1).getCellStyle();
                            cell.setCellStyle(style);
                            cell.setCellType(Cell.CELL_TYPE_BLANK);
                            
                            
                        }
                    }
                    
                    valueCellPositions.add(new CellAddress(cell));
                    
                    // セルの値を出力する
                    Utils.setPositionWithMapColumn(cell.getColumnIndex(), cell.getRowIndex(), record, property.getName(), headerInfo.getLabel());
                    Utils.setLabelWithMapColumn(headerInfo.getLabel(), record, property.getName(), headerInfo.getLabel());
                    try {
                        Object itemValue = property.getValueOfMap(headerInfo.getLabel(), record);
                        converter.toCell(property, itemValue, sheet, cell.getColumnIndex(), cell.getRowIndex(), config);
                        
                    } catch(TypeBindException e) {
                        
                        work.addTypeBindError(e, cell, String.format("%s[%s]", property.getName(), headerInfo.getLabel()), headerInfo.getLabel());
                        if(!config.isContinueTypeBindFailure()) {
                            throw e;
                        }
                    }
                    
                    recordOperation.setupCellPositoin(cell);
                }
                
            }
        }
        
    }
    
    @SuppressWarnings("unchecked")
    private int saveNestedRecords(final Sheet sheet, final List<RecordHeader> headers, final List<CellAddress> valueCellPositions,
            final XlsHorizontalRecords anno,
            final CellAddress beginPositoin,
            final Object record,
            final XlsMapperConfig config, final SavingWorkObject work,
            final List<CellRangeAddress> mergedRanges, final RecordOperation recordOperation,
            final List<Integer> insertRowsIdx) throws XlsMapperException {
        
        int skipSize = 0;
        
        final List<FieldAdaptor> nestedProperties = Utils.getSavingNestedRecordsProperties(record.getClass(), work.getAnnoReader());
        for(FieldAdaptor property : nestedProperties) {
            
            final XlsNestedRecords nestedAnno = property.getSavingAnnotation(XlsNestedRecords.class);
            final Class<?> clazz = property.getTargetClass();
            if(Collection.class.isAssignableFrom(clazz)) {
                // mapping by one-to-many
                
                Class<?> recordClass = nestedAnno.recordClass();
                if(recordClass == Object.class) {
                    recordClass = property.getSavingGenericClassType();
                }
                
                Collection<Object> value = (Collection<Object>) property.getValue(record);
                if(value == null) {
                    // dummy empty record
                    value = (Collection<Object>) Arrays.asList(config.createBean(recordClass));
                }
                
                final List<Object> list = Utils.convertCollectionToList(value);
                final AtomicInteger nestedRecordSize = new AtomicInteger(0);
                saveRecords(sheet, headers, anno, beginPositoin, nestedRecordSize, property, recordClass, list,
                        config, work, mergedRanges, recordOperation, insertRowsIdx);
                
                if(skipSize < list.size()) {
                    if(nestedRecordSize.get() > 0) {
                        skipSize = nestedRecordSize.get() - skipSize;
                    } else {
                        skipSize = list.size();
                    }
                }
                
                processSavingNestedMergedRecord(sheet, skipSize, valueCellPositions);
                
            } else if(clazz.isArray()) {
                
                // mapping by one-to-many
                
                Class<?> recordClass = nestedAnno.recordClass();
                if(recordClass == Object.class) {
                    recordClass = property.getSavingGenericClassType();
                }
                
                Object[] value = (Object[])property.getValue(record);
                if(value == null) {
                    // dummy empty record
                    value = new Object[]{config.createBean(recordClass)};
                }
                
                final List<Object> list = Arrays.asList(value);
                final AtomicInteger nestedRecordSize = new AtomicInteger(0);
                saveRecords(sheet, headers, anno, beginPositoin, nestedRecordSize, property, recordClass, list,
                        config, work, mergedRanges, recordOperation, insertRowsIdx);
                
                if(nestedRecordSize.get() > 0) {
                    skipSize = nestedRecordSize.get() - skipSize;
                } else {
                    skipSize = list.size();
                }
                
                processSavingNestedMergedRecord(sheet, skipSize, valueCellPositions);
                
            } else {
                
                // mapping by one-to-many
                Class<?> recordClass = anno.recordClass();
                if(recordClass == Object.class) {
                    recordClass = property.getTargetClass();
                }
                
                Object value = property.getValue(record);
                if(value == null) {
                    // dummy empty record
                    value = config.createBean(recordClass);
                }
                
                List<Object> list = Arrays.asList(value);
                final AtomicInteger nestedRecordSize = new AtomicInteger(0);
                saveRecords(sheet, headers, anno, beginPositoin, nestedRecordSize, property, recordClass, list,
                        config, work, mergedRanges, recordOperation, insertRowsIdx);
                
                if(nestedRecordSize.get() > 0) {
                    skipSize = nestedRecordSize.get() - skipSize;
                } else {
                    skipSize = list.size();
                }
                
            }
        }
        
        return skipSize;
    }
    
    /**
     * ネストしたレコードの親のセルを結合する
     * @param sheet シート
     * @param mergedSize 結合するセルのサイズ
     * @param valueCellPositions 結合する開始位置のセルのアドレス
     */
    private void processSavingNestedMergedRecord(final Sheet sheet, final int mergedSize,
            final List<CellAddress> valueCellPositions) {
        
        // ネストした場合、上のセルのスタイルをコピーして、結合する
        for(CellAddress position : valueCellPositions) {
            Cell valueCell = POIUtils.getCell(sheet, position);
            if(valueCell == null) {
                continue;
            }
            
            final CellStyle style = valueCell.getCellStyle();
            
            // 結合するセルに対して、上のセルのスタイルをコピーする。
            // 行を挿入するときなどに必要になるため、スタイルを設定する。
            for(int i=1; i < mergedSize; i++) {
                Cell mergedCell = POIUtils.getCell(sheet, position.getColumn(), position.getRow() + i);
                mergedCell.setCellStyle(style);
                mergedCell.setCellType(Cell.CELL_TYPE_BLANK);
            }
            
            final CellRangeAddress range = new CellRangeAddress(position.getRow(), position.getRow()+ mergedSize-1,
                    position.getColumn(), position.getColumn());
            
            // 既に結合済みのセルがある場合、外す。
            for(int rowIdx=range.getFirstRow(); rowIdx <= range.getLastRow(); rowIdx++) {
                CellRangeAddress r = POIUtils.getMergedRegion(sheet, rowIdx, position.getColumn());
                if(r != null) {
                    POIUtils.removeMergedRange(sheet, r);
                }
            }
            
            sheet.addMergedRegion(range);
        }
        
    }
    
    /**
     * セルの入力規則の範囲を修正する。
     * @param sheet
     * @param recordOperation
     */
    private void correctDataValidation(final Sheet sheet, final RecordOperation recordOperation) {
        
        if(!POIUtils.AVAILABLE_METHOD_SHEET_DAVA_VALIDATION) {
            return;
        }
        
        if(recordOperation.isNotExecuteRecordOperation()) {
            return;
        }
        
        //TODO: セルの結合も考慮する
        
        // 操作をしていないセルの範囲の取得
        final CellRangeAddress notOperateRange = new CellRangeAddress(
                recordOperation.getTopLeftPoisitoin().y,
                recordOperation.getBottomRightPosition().y - recordOperation.getCountInsertRecord(),
                recordOperation.getTopLeftPoisitoin().x,
                recordOperation.getBottomRightPosition().x
                );
        
        final List<? extends DataValidation> list = sheet.getDataValidations();
        for(DataValidation validation : list) {
            
            final CellRangeAddressList region = validation.getRegions().copy();
            boolean changedRange = false;
            for(CellRangeAddress range : region.getCellRangeAddresses()) {
                
                if(notOperateRange.isInRange(range.getFirstRow(), range.getFirstColumn())) {
                    // 自身のセルの範囲の場合は、行の範囲を広げる
                    range.setLastRow(recordOperation.getBottomRightPosition().y);
                    changedRange = true;
                    
                } else if(notOperateRange.getLastRow() < range.getFirstRow()) {
                    // 自身のセルの範囲より下方にあるセルの範囲の場合、行の挿入や削除に影響を受けているので修正する。
                    if(recordOperation.isInsertRecord()) {
                        range.setFirstRow(range.getFirstRow() + recordOperation.getCountInsertRecord());
                        range.setLastRow(range.getLastRow() + recordOperation.getCountInsertRecord());
                        
                    } else if(recordOperation.isDeleteRecord()) {
                        range.setFirstRow(range.getFirstRow() - recordOperation.getCountDeleteRecord());
                        range.setLastRow(range.getLastRow() - recordOperation.getCountDeleteRecord());
                        
                    }
                    changedRange = true;
                }
                
            }
            
            // 修正した規則を、更新する。
            if(changedRange) {
                boolean updated = POIUtils.updateDataValidationRegion(sheet, validation.getRegions(), region);
                assert updated;
            }
        }
        
    }
    
    /**
     * 名前の定義の範囲を修正する。
     * @param sheet
     * @param recordOperation
     */
    private void correctNameRange(final Sheet sheet, final RecordOperation recordOperation) {
        
        if(recordOperation.isNotExecuteRecordOperation()) {
            return;
        }
        
        final Workbook workbook = sheet.getWorkbook();
        final int numName = workbook.getNumberOfNames();
        if(numName == 0) {
            return;
        }
        
        // 操作をしていないセルの範囲の取得
        final CellRangeAddress notOperateRange = new CellRangeAddress(
                recordOperation.getTopLeftPoisitoin().y,
                recordOperation.getBottomRightPosition().y - recordOperation.getCountInsertRecord(),
                recordOperation.getTopLeftPoisitoin().x,
                recordOperation.getBottomRightPosition().x
                );
        
        for(int i=0; i < numName; i++) {
            final Name name = workbook.getNameAt(i);
            
            if(name.isDeleted() || name.isFunctionName()) {
                // 削除されている場合、関数の場合はスキップ
                continue;
            }
            
            if(!sheet.getSheetName().equals(name.getSheetName())) {
                // 自身のシートでない名前は、修正しない。
                continue;
            }
            
            AreaReference areaRef = new AreaReference(name.getRefersToFormula());
            CellReference firstCellRef = areaRef.getFirstCell();
            CellReference lastCellRef = areaRef.getLastCell();
            
            if(notOperateRange.isInRange(firstCellRef.getRow(), firstCellRef.getCol())) {
                // 自身のセルの範囲の場合は、行の範囲を広げる。
                
                lastCellRef= new CellReference(
                        lastCellRef.getSheetName(),
                        recordOperation.getBottomRightPosition().y, lastCellRef.getCol(),
                        lastCellRef.isRowAbsolute(), lastCellRef.isColAbsolute());
                areaRef = new AreaReference(firstCellRef, lastCellRef);
                
                // 修正した範囲を再設定する
                name.setRefersToFormula(areaRef.formatAsString());
                
            } else if(notOperateRange.getLastRow() < firstCellRef.getRow()) {
                /*
                 * 名前の定義の場合、自身のセルの範囲より下方にあるセルの範囲の場合、
                 * 自動的に修正されるため、修正は必要なし。
                 */
                
            }
            
        }
        
    }
    
    /**
     * セルのコメントを全て取得する。その際に、セルからコメントを削除する。
     * @param sheet
     * @return
     */
    private List<CellCommentStore> loadCommentAndRemove(final Sheet sheet) {
        
        final List<CellCommentStore> list = new ArrayList<>();
        
        final int maxRow = POIUtils.getRows(sheet);
        for(int rowIndex=0; rowIndex < maxRow; rowIndex++) {
            final Row row = sheet.getRow(rowIndex);
            if(row == null) {
                continue;
            }
            
            final short maxCol = row.getLastCellNum();
            for(short colIndex=0; colIndex < maxCol; colIndex++) {
                
                final Cell cell = row.getCell(colIndex);
                if(cell == null) {
                    continue;
                }
                
                CellCommentStore commentStore = CellCommentStore.getAndRemove(cell);
                if(commentStore != null) {
                    list.add(commentStore);
                }
            }
            
        }
        
        return list;
    }
    
    /**
     * セルのコメントを再設定する。
     * @param sheet
     * @param recordOperation
     * @param commentStoreList
     */
    private void correctComment(final Sheet sheet, final RecordOperation recordOperation,
            final List<CellCommentStore> commentStoreList) {
        
        if(commentStoreList.isEmpty()) {
            return;
        }
        
        if(!recordOperation.isInsertRecord() && !recordOperation.isDeleteRecord()) {
            return;
        }
        
        // 操作をしていないセルの範囲の取得
        final CellRangeAddress notOperateRange = new CellRangeAddress(
                recordOperation.getTopLeftPoisitoin().y,
                recordOperation.getBottomRightPosition().y - recordOperation.getCountInsertRecord(),
                recordOperation.getTopLeftPoisitoin().x,
                recordOperation.getBottomRightPosition().x
                );
        
        for(CellCommentStore commentStore : commentStoreList) {
            
            if(notOperateRange.getLastRow() >= commentStore.getRow()) {
                // 行の追加・削除をしていない範囲の場合
                commentStore.set(sheet);
                
            } else {
                // 自身のセルの範囲より下方にあるセルの範囲の場合、行の挿入や削除に影響を受けているので修正する。
                if(recordOperation.isInsertRecord()) {
                    commentStore.addRow(recordOperation.getCountInsertRecord());
                    commentStore.set(sheet);
                    
                } else if(recordOperation.isDeleteRecord()) {
                    commentStore.addRow(-recordOperation.getCountDeleteRecord());
                    commentStore.set(sheet);
                    
                }
                
            }
            
        }
        
    }
}
