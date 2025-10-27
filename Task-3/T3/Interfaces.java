package T3;

public interface Interfaces {
    // Интерфейсы из диаграммы
    interface IProductPart {
        String getName();
    }

    interface ILineStep {
        IProductPart buildProductPart();
    }

    interface IProduct {
        void installFirstPart(IProductPart part);
        void installSecondPart(IProductPart part);
        void installThirdPart(IProductPart part);
        String getInfo();

        boolean isAssembled();
    }

    interface IAssemblyLine {
        IProduct assembleProduct(IProduct product);
    }
}
