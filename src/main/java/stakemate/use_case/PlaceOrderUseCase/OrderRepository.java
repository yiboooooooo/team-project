package stakemate.use_case.PlaceOrderUseCase;

import stakemate.engine.BookOrder;

public interface OrderRepository {
    void save(BookOrder order);

    BookOrder findById(String buyOrderId);
}

