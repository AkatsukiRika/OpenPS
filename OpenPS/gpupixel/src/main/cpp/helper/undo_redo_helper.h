/*
* UndoRedoHelper
*
* Created by Rika on 2024/9/26.
* CopyRight © 2024 Rika. All rights reserved.
*/

#pragma once

#include <vector>
#include <memory>
#include "abstract_record.h"
#include "gpupixel_macros.h"
#include "util.h"

NS_GPUPIXEL_BEGIN

class GPUPIXEL_API UndoRedoHelper {
public:
  UndoRedoHelper();

  void addRecord(const AbstractRecord& record);
  bool canUndo();
  bool canRedo();
  std::shared_ptr<AbstractRecord> undo();
  std::shared_ptr<AbstractRecord> redo();

private:
  std::vector<std::shared_ptr<AbstractRecord>> recordList;
  int currentIndex = 0;

  void addEmptyRecord();
  std::shared_ptr<AbstractRecord> getEmptyRecord();
};

NS_GPUPIXEL_END