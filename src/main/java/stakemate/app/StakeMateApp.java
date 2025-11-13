package stakemate.app;

import javax.swing.SwingUtilities;

import stakemate.data_access.in_memory.FakeOrderBookGateway;
import stakemate.data_access.in_memory.InMemoryMarketRepository;
import stakemate.data_access.in_memory.InMemoryMatchRepository;
import stakemate.interface_adapter.view_market.SwingViewMarketsPresenter;
import stakemate.interface_adapter.view_market.ViewMarketController;
import stakemate.use_case.view_market.ViewMarketInteractor;
import stakemate.view.MarketsFrame;

public final class StakeMateApp {

    private StakeMateApp() {
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            // Infrastructure
            InMemoryMatchRepository matchRepository = new InMemoryMatchRepository();
            InMemoryMarketRepository marketRepository = new InMemoryMarketRepository();
            FakeOrderBookGateway orderBookGateway = new FakeOrderBookGateway();

            // UI (Swing)
            MarketsFrame frame = new MarketsFrame();

            // Presenter
            SwingViewMarketsPresenter presenter = new SwingViewMarketsPresenter(frame);

            // Use case / interactor
            ViewMarketInteractor interactor =
                    new ViewMarketInteractor(matchRepository, marketRepository, orderBookGateway, presenter);

            // Controller
            ViewMarketController controller = new ViewMarketController(interactor);
            frame.setController(controller);

            frame.setVisible(true);

            // trigger initial load
            controller.refresh();
        });
    }
}
