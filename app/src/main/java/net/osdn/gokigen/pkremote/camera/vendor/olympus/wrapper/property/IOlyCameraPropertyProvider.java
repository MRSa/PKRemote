package net.osdn.gokigen.pkremote.camera.vendor.olympus.wrapper.property;

import java.util.List;
import java.util.Map;
import java.util.Set;

public interface IOlyCameraPropertyProvider
{
    // 現在利用可能なカメラプロパティ名のリスト
    Set<String> getCameraPropertyNames();

    // カメラプロパティに設定されている値を取得 : 戻りは "<WB/WB_AUTO>" な感じ
    String getCameraPropertyValue(String name);

    // カメラプロパティに設定されている値を一括取得
    Map<String, String> getCameraPropertyValues(Set<String> names);

    // カメラプロパティの名称
    String getCameraPropertyTitle(String name);

    // 設定可能なカメラプロパティ値のリストを取得する
    List<String> getCameraPropertyValueList(String name);

    // カメラプロパティの表示値
    String getCameraPropertyValueTitle(String propertyValue);

    // カメラプロパティに値を設定
    void setCameraPropertyValue	(String name, String value);

    // カメラプロパティに値を一括設定
    void setCameraPropertyValues(Map<String, String> values);

    // カメラプロパティに値を設定できるかを検査
    boolean canSetCameraProperty(String name);
}
