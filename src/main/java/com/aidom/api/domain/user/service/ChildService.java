package com.aidom.api.domain.user.service;

import com.aidom.api.domain.user.entity.Child;
import com.aidom.api.domain.user.repository.ChildRepository;
import com.aidom.api.global.error.CustomException;
import com.aidom.api.global.error.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ChildService {

  private final ChildRepository childRepository;

  public Child getChildById(Long childId) {
    return childRepository
        .findById(childId)
        .orElseThrow(() -> new CustomException(ErrorCode.CHILD_NOT_FOUND));
  }
}
