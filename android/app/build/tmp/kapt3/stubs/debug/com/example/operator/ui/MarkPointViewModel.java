package com.example.operator.ui;

/**
 * Хранит состояние текущей отметки точки между шагами диалогов.
 */
@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000*\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\b\u0005\n\u0002\u0018\u0002\n\u0002\b\u0005\n\u0002\u0018\u0002\n\u0002\b\u0005\n\u0002\u0010\u0002\n\u0000\u0018\u00002\u00020\u0001B\u0005\u00a2\u0006\u0002\u0010\u0002J\u0006\u0010\u0015\u001a\u00020\u0016R\u001c\u0010\u0003\u001a\u0004\u0018\u00010\u0004X\u0086\u000e\u00a2\u0006\u000e\n\u0000\u001a\u0004\b\u0005\u0010\u0006\"\u0004\b\u0007\u0010\bR\u001c\u0010\t\u001a\u0004\u0018\u00010\nX\u0086\u000e\u00a2\u0006\u000e\n\u0000\u001a\u0004\b\u000b\u0010\f\"\u0004\b\r\u0010\u000eR\u001c\u0010\u000f\u001a\u0004\u0018\u00010\u0010X\u0086\u000e\u00a2\u0006\u000e\n\u0000\u001a\u0004\b\u0011\u0010\u0012\"\u0004\b\u0013\u0010\u0014\u00a8\u0006\u0017"}, d2 = {"Lcom/example/operator/ui/MarkPointViewModel;", "Landroidx/lifecycle/ViewModel;", "()V", "direction", "Lcom/example/operator/model/Direction;", "getDirection", "()Lcom/example/operator/model/Direction;", "setDirection", "(Lcom/example/operator/model/Direction;)V", "location", "Landroid/location/Location;", "getLocation", "()Landroid/location/Location;", "setLocation", "(Landroid/location/Location;)V", "objectType", "Lcom/example/operator/model/ObjectType;", "getObjectType", "()Lcom/example/operator/model/ObjectType;", "setObjectType", "(Lcom/example/operator/model/ObjectType;)V", "reset", "", "app_debug"})
public final class MarkPointViewModel extends androidx.lifecycle.ViewModel {
    @org.jetbrains.annotations.Nullable()
    private android.location.Location location;
    @org.jetbrains.annotations.Nullable()
    private com.example.operator.model.ObjectType objectType;
    @org.jetbrains.annotations.Nullable()
    private com.example.operator.model.Direction direction;
    
    public MarkPointViewModel() {
        super();
    }
    
    @org.jetbrains.annotations.Nullable()
    public final android.location.Location getLocation() {
        return null;
    }
    
    public final void setLocation(@org.jetbrains.annotations.Nullable()
    android.location.Location p0) {
    }
    
    @org.jetbrains.annotations.Nullable()
    public final com.example.operator.model.ObjectType getObjectType() {
        return null;
    }
    
    public final void setObjectType(@org.jetbrains.annotations.Nullable()
    com.example.operator.model.ObjectType p0) {
    }
    
    @org.jetbrains.annotations.Nullable()
    public final com.example.operator.model.Direction getDirection() {
        return null;
    }
    
    public final void setDirection(@org.jetbrains.annotations.Nullable()
    com.example.operator.model.Direction p0) {
    }
    
    public final void reset() {
    }
}