package com.philpot.camera.helper;

class ImagePreviewEdgePair {
    public ImagePreviewEdge primary;
    public ImagePreviewEdge secondary;

    public ImagePreviewEdgePair(ImagePreviewEdge edge1, ImagePreviewEdge edge2) {
        primary = edge1;
        secondary = edge2;
    }
}
