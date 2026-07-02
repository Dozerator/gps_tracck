package com.example.operator.model;

/**
 * Уровень угрозы. Хранится локально в очереди точек (см. [com.example.operator.data.local.entity.PendingPointEntity]).
 * В текущем UI-флоу шага выбора уровня угрозы нет, поэтому используется значение по умолчанию
 * [OBSERVATION]; backend пока не принимает это поле, и оно не уходит в сетевой запрос.
 */
@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000\u0012\n\u0002\u0018\u0002\n\u0002\u0010\u0010\n\u0000\n\u0002\u0010\u000e\n\u0002\b\t\b\u0086\u0081\u0002\u0018\u00002\b\u0012\u0004\u0012\u00020\u00000\u0001B\u0017\b\u0002\u0012\u0006\u0010\u0002\u001a\u00020\u0003\u0012\u0006\u0010\u0004\u001a\u00020\u0003\u00a2\u0006\u0002\u0010\u0005R\u0011\u0010\u0002\u001a\u00020\u0003\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0006\u0010\u0007R\u0011\u0010\u0004\u001a\u00020\u0003\u00a2\u0006\b\n\u0000\u001a\u0004\b\b\u0010\u0007j\u0002\b\tj\u0002\b\nj\u0002\b\u000b\u00a8\u0006\f"}, d2 = {"Lcom/example/operator/model/ThreatLevel;", "", "apiValue", "", "label", "(Ljava/lang/String;ILjava/lang/String;Ljava/lang/String;)V", "getApiValue", "()Ljava/lang/String;", "getLabel", "OBSERVATION", "ATTENTION", "THREAT", "app_debug"})
public enum ThreatLevel {
    /*public static final*/ OBSERVATION /* = new OBSERVATION(null, null) */,
    /*public static final*/ ATTENTION /* = new ATTENTION(null, null) */,
    /*public static final*/ THREAT /* = new THREAT(null, null) */;
    @org.jetbrains.annotations.NotNull()
    private final java.lang.String apiValue = null;
    @org.jetbrains.annotations.NotNull()
    private final java.lang.String label = null;
    
    ThreatLevel(java.lang.String apiValue, java.lang.String label) {
    }
    
    @org.jetbrains.annotations.NotNull()
    public final java.lang.String getApiValue() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final java.lang.String getLabel() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public static kotlin.enums.EnumEntries<com.example.operator.model.ThreatLevel> getEntries() {
        return null;
    }
}