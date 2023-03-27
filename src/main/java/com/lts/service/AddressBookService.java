package com.lts.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.lts.entity.AddressBook;

import java.util.List;

public interface AddressBookService extends IService<AddressBook> {

    List<AddressBook> addressList(Long userId);

    void setDefaultAddress(Long id, Long userId);

    AddressBook getAddressById(Long id);

    void deleteById(Long id);

    void updateAddress(AddressBook addressBook);

    AddressBook getDefaultAddr(Long currentId);
}
